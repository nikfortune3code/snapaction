import express from 'express';
import multer from 'multer';
import cors from 'cors';
import dotenv from 'dotenv';
import { GoogleGenAI, Type } from '@google/genai';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3001;

// Initialize CORS and JSON middleware
app.use(cors());
app.use(express.json());

// 1. Configure Multer with Memory Storage & Validation (JPEG, PNG, WebP, max 5MB)
const storage = multer.memoryStorage();

const fileFilter = (req, file, cb) => {
  const allowedMimeTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
  if (allowedMimeTypes.includes(file.mimetype)) {
    cb(null, true);
  } else {
    const error = new Error('INVALID_FILE_TYPE: Only JPEG, PNG, and WebP images are allowed.');
    error.code = 'INVALID_FILE_TYPE';
    cb(error, false);
  }
};

const upload = multer({
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024 // 5 MB Limit
  },
  fileFilter: fileFilter
});

// 2. Define Gemini Response Schema for Strict JSON Output
const responseSchema = {
  type: Type.OBJECT,
  properties: {
    detected_category: {
      type: Type.STRING,
      enum: ['BILL_RECEIPT', 'GROCERY_LIST', 'FOOD_DISH', 'PACKAGED_ITEM', 'OTHER'],
      description: 'The primary classification category of the image'
    },
    confidence_score: {
      type: Type.NUMBER,
      description: 'Confidence score of category classification between 0.0 and 1.0'
    },
    summary_title: {
      type: Type.STRING,
      description: 'A concise summary title describing the image content'
    },
    expense_details: {
      type: Type.OBJECT,
      properties: {
        is_expense: { type: Type.BOOLEAN },
        merchant: { type: Type.STRING, nullable: true },
        total_amount: { type: Type.NUMBER, nullable: true },
        currency: { type: Type.STRING, nullable: true },
        date: { type: Type.STRING, nullable: true }
      },
      required: ['is_expense']
    },
    extracted_items: {
      type: Type.ARRAY,
      items: {
        type: Type.OBJECT,
        properties: {
          name: { type: Type.STRING },
          quantity: { type: Type.STRING, nullable: true },
          estimated_price: { type: Type.NUMBER, nullable: true }
        },
        required: ['name']
      }
    },
    recipe_details: {
      type: Type.OBJECT,
      properties: {
        dish_name: { type: Type.STRING, nullable: true },
        estimated_ingredients_required: {
          type: Type.ARRAY,
          items: { type: Type.STRING }
        },
        notes: { type: Type.STRING, nullable: true }
      },
      required: ['estimated_ingredients_required']
    }
  },
  required: ['detected_category', 'confidence_score', 'summary_title', 'expense_details', 'extracted_items', 'recipe_details']
};

/**
 * Robust JSON parsing fallback helper function.
 * Strips code fences, sanitizes whitespace, and guarantees schema defaults.
 */
function parseAndValidateJsonResponse(rawText) {
  let cleaned = rawText.trim();
  
  // Remove markdown code fences if present (e.g., ```json ... ```)
  cleaned = cleaned.replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/i, '').trim();

  try {
    const parsed = JSON.parse(cleaned);

    // Apply strict fallback structure to prevent null pointer exceptions
    return {
      detected_category: ['BILL_RECEIPT', 'GROCERY_LIST', 'FOOD_DISH', 'PACKAGED_ITEM', 'OTHER'].includes(parsed.detected_category)
        ? parsed.detected_category
        : 'OTHER',
      confidence_score: typeof parsed.confidence_score === 'number' ? Math.min(Math.max(parsed.confidence_score, 0), 1) : 0.85,
      summary_title: parsed.summary_title || 'Parsed Image Content',
      expense_details: {
        is_expense: Boolean(parsed.expense_details?.is_expense),
        merchant: parsed.expense_details?.merchant || null,
        total_amount: typeof parsed.expense_details?.total_amount === 'number' ? parsed.expense_details.total_amount : null,
        currency: parsed.expense_details?.currency || 'USD',
        date: parsed.expense_details?.date || null
      },
      extracted_items: Array.isArray(parsed.extracted_items)
        ? parsed.extracted_items.map(item => ({
            name: item.name || 'Unknown Item',
            quantity: item.quantity || null,
            estimated_price: typeof item.estimated_price === 'number' ? item.estimated_price : null
          }))
        : [],
      recipe_details: {
        dish_name: parsed.recipe_details?.dish_name || null,
        estimated_ingredients_required: Array.isArray(parsed.recipe_details?.estimated_ingredients_required)
          ? parsed.recipe_details.estimated_ingredients_required
          : [],
        notes: parsed.recipe_details?.notes || null
      }
    };
  } catch (parseErr) {
    console.error('JSON parsing failed. Returning safe fallback structure. Error:', parseErr.message);
    return {
      detected_category: 'OTHER',
      confidence_score: 0.50,
      summary_title: 'Unstructured Image Data',
      expense_details: { is_expense: false, merchant: null, total_amount: null, currency: null, date: null },
      extracted_items: [],
      recipe_details: { dish_name: null, estimated_ingredients_required: [], notes: 'Fallback response due to JSON parsing error.' }
    };
  }
}

// 3. Image Analysis API Endpoint
app.post('/api/analyze-image', (req, res, next) => {
  upload.single('image')(req, res, (err) => {
    if (err) {
      if (err.code === 'LIMIT_FILE_SIZE') {
        return res.status(400).json({
          error: 'FILE_SIZE_LIMIT_EXCEEDED',
          message: 'The uploaded file exceeds the 5MB size limit.'
        });
      }
      if (err.code === 'INVALID_FILE_TYPE') {
        return res.status(400).json({
          error: 'INVALID_FILE_TYPE',
          message: err.message
        });
      }
      return res.status(400).json({
        error: 'UPLOAD_ERROR',
        message: err.message || 'Error uploading file.'
      });
    }
    next();
  });
}, async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        error: 'MISSING_FILE',
        message: 'Please provide an image file in the request under key "image".'
      });
    }

    const apiKey = process.env.GEMINI_API_KEY || process.env.VITE_GEMINI_API_KEY;
    if (!apiKey) {
      return res.status(500).json({
        error: 'MISSING_API_KEY',
        message: 'GEMINI_API_KEY environment variable is not configured on the server.'
      });
    }

    // Initialize Google Gen AI Client
    const ai = new GoogleGenAI({ apiKey });

    // Prepare image payload for Gemini Vision model
    const imageBase64 = req.file.buffer.toString('base64');
    const imagePart = {
      inlineData: {
        data: imageBase64,
        mimeType: req.file.mimetype
      }
    };

    const promptText = `Analyze the uploaded image or screenshot carefully.
Categorize it into one of: BILL_RECEIPT, GROCERY_LIST, FOOD_DISH, PACKAGED_ITEM, OTHER.
Extract all structured expenses, list items, or inferred recipe ingredients accurately.
Return strictly structured JSON adhering to the specified responseSchema.`;

    // Call Gemini 2.5 Flash model with responseSchema
    const response = await ai.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: [imagePart, promptText],
      config: {
        responseMimeType: 'application/json',
        responseSchema: responseSchema,
        temperature: 0.1
      }
    });

    const rawResultText = response.text;
    const finalData = parseAndValidateJsonResponse(rawResultText);

    return res.status(200).json({
      success: true,
      data: finalData
    });

  } catch (error) {
    console.error('Error in /api/analyze-image:', error);
    return res.status(500).json({
      error: 'AI_PROCESSING_ERROR',
      message: 'Failed to process image with Gemini Vision API.',
      details: error.message
    });
  }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', engine: 'SnapAction Gemini Vision Server' });
});

// Start Server
app.listen(PORT, () => {
  console.log(`⚡ SnapAction Vision Backend Server running on http://localhost:${PORT}`);
});
