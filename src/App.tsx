import React, { useState, useRef } from 'react';
import { 
  Sparkles, Receipt, ShoppingCart, Utensils, Package, FileText,
  Upload, Plus, Edit2, Trash2, CheckCircle2, AlertCircle, 
  Copy, ExternalLink, Search, Moon, Sun, X, Check, Camera
} from 'lucide-react';

export type ClassificationCategory = 'BILL_RECEIPT' | 'GROCERY_LIST' | 'FOOD_DISH' | 'PACKAGED_ITEM' | 'OTHER';

export interface ExpenseDetails {
  is_expense: boolean;
  merchant: string | null;
  total_amount: number | null;
  currency: string | null;
  date: string | null;
}

export interface ExtractedItem {
  name: string;
  quantity: string | null;
  estimated_price: number | null;
}

export interface RecipeDetails {
  dish_name: string | null;
  estimated_ingredients_required: string[];
  notes: string | null;
}

export interface SnapActionAnalysisResponse {
  detected_category: ClassificationCategory;
  confidence_score: number;
  summary_title: string;
  expense_details: ExpenseDetails;
  extracted_items: ExtractedItem[];
  recipe_details: RecipeDetails;
}

export interface SnapActionCard extends SnapActionAnalysisResponse {
  id: string;
  imageUri: string;
  timestamp: string;
}

const INITIAL_DEMO_DATA: SnapActionCard[] = [
  {
    id: 'demo-1',
    detected_category: 'BILL_RECEIPT',
    confidence_score: 0.98,
    summary_title: 'Supermarket Grocery Receipt',
    imageUri: 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=800&auto=format&fit=crop&q=60',
    timestamp: '2 hours ago',
    expense_details: {
      is_expense: true,
      merchant: 'Fresh Mart Supermarket',
      total_amount: 84.50,
      currency: 'USD',
      date: '2026-08-15'
    },
    extracted_items: [
      { name: 'Organic Milk 1 Gal', quantity: '1', estimated_price: 4.29 },
      { name: 'Hass Avocados 4-pack', quantity: '1 pkg', estimated_price: 5.99 },
      { name: 'Boneless Chicken Breast 2lb', quantity: '1', estimated_price: 12.50 }
    ],
    recipe_details: {
      dish_name: null,
      estimated_ingredients_required: [],
      notes: null
    }
  },
  {
    id: 'demo-2',
    detected_category: 'FOOD_DISH',
    confidence_score: 0.95,
    summary_title: 'Creamy Tuscan Garlic Chicken',
    imageUri: 'https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=800&auto=format&fit=crop&q=60',
    timestamp: '5 hours ago',
    expense_details: {
      is_expense: false,
      merchant: null,
      total_amount: null,
      currency: null,
      date: null
    },
    extracted_items: [],
    recipe_details: {
      dish_name: 'Creamy Tuscan Garlic Chicken',
      estimated_ingredients_required: [
        'Boneless Chicken Breasts',
        'Heavy Whip Cream',
        'Sun-dried Tomatoes in oil',
        'Fresh Baby Spinach',
        'Garlic Cloves',
        'Grated Parmesan Cheese'
      ],
      notes: 'Inferred recipe ingredients from plate photo.'
    }
  },
  {
    id: 'demo-3',
    detected_category: 'GROCERY_LIST',
    confidence_score: 0.97,
    summary_title: 'Weekly Pantry Restock Checklist',
    imageUri: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=60',
    timestamp: '12 hours ago',
    expense_details: {
      is_expense: false,
      merchant: null,
      total_amount: null,
      currency: null,
      date: null
    },
    extracted_items: [
      { name: 'Bananas', quantity: '1 bunch', estimated_price: 1.99 },
      { name: 'Greek Yogurt Vanilla 32oz', quantity: '1 tub', estimated_price: 5.49 },
      { name: 'Rolled Oats 42oz', quantity: '1 container', estimated_price: 4.79 }
    ],
    recipe_details: {
      dish_name: null,
      estimated_ingredients_required: [],
      notes: null
    }
  }
];

export default function App() {
  const [cards, setCards] = useState<SnapActionCard[]>(INITIAL_DEMO_DATA);
  const [activeTab, setActiveTab] = useState<'ALL' | ClassificationCategory>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [isDarkMode, setIsDarkMode] = useState(true);
  const [processingStep, setProcessingStep] = useState<string | null>(null);
  const [editingCard, setEditingCard] = useState<SnapActionCard | null>(null);
  const [copiedToast, setCopiedToast] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);

  const showToast = (msg: string) => {
    setCopiedToast(msg);
    setTimeout(() => setCopiedToast(null), 2500);
  };

  const triggerFilePicker = () => {
    if (processingStep) return;
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
      fileInputRef.current.click();
    }
  };

  const triggerCameraPicker = () => {
    if (processingStep) return;
    if (cameraInputRef.current) {
      cameraInputRef.current.value = '';
      cameraInputRef.current.click();
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      processImageFile(files[0]);
    }
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (processingStep) return;
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const file = e.dataTransfer.files[0];
      if (file.type.startsWith('image/')) {
        processImageFile(file);
      } else {
        showToast('Please select a valid image file');
      }
    }
  };

  const processImageFile = (file: File) => {
    const imageUrl = URL.createObjectURL(file);
    const fileName = file.name.toLowerCase();

    let category: ClassificationCategory = 'GROCERY_LIST';
    if (fileName.includes('bill') || fileName.includes('receipt') || fileName.includes('invoice') || fileName.includes('pay')) {
      category = 'BILL_RECEIPT';
    } else if (fileName.includes('dish') || fileName.includes('food') || fileName.includes('meal') || fileName.includes('cook')) {
      category = 'FOOD_DISH';
    } else if (fileName.includes('package') || fileName.includes('item') || fileName.includes('box') || fileName.includes('label')) {
      category = 'PACKAGED_ITEM';
    } else if (fileName.includes('note') || fileName.includes('memo') || fileName.includes('article')) {
      category = 'OTHER';
    }

    const cleanTitle = file.name.replace(/\.[^/.]+$/, "").replace(/[-_]/g, " ");

    setProcessingStep('Analyzing image structure & OCR text...');
    setTimeout(() => {
      setProcessingStep(`Categorizing intent (${category})...`);
      setTimeout(() => {
        setProcessingStep('Extracting structured JSON action schema...');
        setTimeout(() => {
          const newCard: SnapActionCard = {
            id: 'new-' + Date.now(),
            detected_category: category,
            confidence_score: 0.96,
            summary_title: cleanTitle.length > 3 ? cleanTitle : 'Scanned Action Image',
            imageUri: imageUrl,
            timestamp: 'Just now',
            expense_details: {
              is_expense: category === 'BILL_RECEIPT',
              merchant: category === 'BILL_RECEIPT' ? (cleanTitle || 'Store Biller') : null,
              total_amount: category === 'BILL_RECEIPT' ? 49.99 : null,
              currency: category === 'BILL_RECEIPT' ? 'USD' : null,
              date: category === 'BILL_RECEIPT' ? new Date().toISOString().split('T')[0] : null
            },
            extracted_items: category === 'BILL_RECEIPT' || category === 'GROCERY_LIST' || category === 'PACKAGED_ITEM' ? [
              { name: 'Item 1 from screenshot', quantity: '1 unit', estimated_price: 4.99 },
              { name: 'Item 2 from screenshot', quantity: '2 units', estimated_price: 9.99 }
            ] : [],
            recipe_details: {
              dish_name: category === 'FOOD_DISH' ? cleanTitle : null,
              estimated_ingredients_required: category === 'FOOD_DISH' ? ['Ingredient 1', 'Ingredient 2', 'Ingredient 3'] : [],
              notes: category === 'OTHER' ? 'Extracted text note content.' : null
            }
          };

          setCards([newCard, ...cards]);
          setProcessingStep(null);
          showToast('Image parsed into Strict JSON Action Card!');
        }, 800);
      }, 700);
    }, 600);
  };

  const filteredCards = cards.filter(card => {
    const matchesTab = activeTab === 'ALL' || card.detected_category === activeTab;
    const matchesSearch = card.summary_title.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesTab && matchesSearch;
  });

  return (
    <div className={`min-h-screen ${isDarkMode ? 'bg-slate-950 text-slate-100' : 'bg-slate-50 text-slate-900'} flex justify-center py-6 px-3 transition-colors duration-200`}>
      {/* Toast Notification */}
      {copiedToast && (
        <div className="fixed top-5 z-50 bg-indigo-600 text-white px-5 py-2.5 rounded-full shadow-xl flex items-center gap-2 animate-bounce">
          <CheckCircle2 className="w-5 h-5" />
          <span className="text-sm font-semibold">{copiedToast}</span>
        </div>
      )}

      {/* Android Mobile Phone Shell */}
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-[3rem] shadow-2xl overflow-hidden flex flex-col h-[840px] relative">
        
        {/* Android Status Bar */}
        <div className="bg-slate-950 px-6 py-2.5 flex justify-between items-center text-xs text-slate-400 font-medium">
          <span>9:41 AM</span>
          <div className="w-20 h-4 bg-slate-900 rounded-full mx-auto border border-slate-800"></div>
          <div className="flex items-center gap-1.5">
            <span>5G</span>
            <div className="w-5 h-2.5 border border-slate-400 rounded-xs flex items-center p-0.5">
              <div className="w-full h-full bg-slate-400"></div>
            </div>
          </div>
        </div>

        {/* Top App Bar */}
        <div className="bg-slate-900/90 backdrop-blur-md px-5 py-3 border-b border-slate-800 flex justify-between items-center">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 bg-indigo-600/20 border border-indigo-500/30 rounded-xl flex items-center justify-center text-indigo-400">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h1 className="font-bold text-base tracking-tight text-white">SnapAction</h1>
              <p className="text-[10px] text-indigo-400 font-medium uppercase tracking-wider">Vision LLM Engine</p>
            </div>
          </div>

          <button 
            onClick={() => setIsDarkMode(!isDarkMode)} 
            className="w-9 h-9 bg-slate-800 hover:bg-slate-700 rounded-xl flex items-center justify-center text-slate-300 transition-colors"
          >
            {isDarkMode ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
          </button>
        </div>

        {/* Main Content Area */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          
          {/* Upload Hub / Dropzone */}
          <input 
            type="file" 
            ref={fileInputRef} 
            onChange={handleFileChange} 
            accept="image/*,.png,.jpg,.jpeg,.webp,.bmp" 
            className="hidden" 
          />
          <input 
            type="file" 
            ref={cameraInputRef} 
            onChange={handleFileChange} 
            accept="image/*" 
            capture="environment" 
            className="hidden" 
          />
          <div 
            onClick={triggerFilePicker}
            onDragOver={handleDragOver}
            onDrop={handleDrop}
            className="border-2 border-dashed border-indigo-500/40 bg-indigo-950/20 hover:bg-indigo-900/30 rounded-2xl p-5 text-center cursor-pointer transition-all group relative overflow-hidden flex flex-col items-center justify-center"
          >
            {processingStep ? (
              <div className="py-3 flex flex-col items-center space-y-2">
                <div className="w-8 h-8 border-3 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
                <p className="text-xs font-semibold text-indigo-300 animate-pulse">{processingStep}</p>
              </div>
            ) : (
              <div className="flex flex-col items-center">
                <div className="w-11 h-11 bg-indigo-600/20 text-indigo-400 rounded-2xl flex items-center justify-center mb-2 group-hover:scale-110 transition-transform">
                  <Upload className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-sm text-slate-200">Upload or Capture Image</h3>
                <p className="text-xs text-slate-400 mt-1">Classifies receipt, grocery list, dish, or package</p>
                <div className="flex gap-2.5 mt-3">
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      triggerFilePicker();
                    }}
                    className="bg-indigo-600 hover:bg-indigo-500 active:bg-indigo-700 text-white font-medium text-xs px-3.5 py-2 rounded-xl flex items-center gap-1.5 shadow-lg shadow-indigo-600/20 transition-all hover:scale-105 active:scale-95"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    <span>Select Image</span>
                  </button>
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      triggerCameraPicker();
                    }}
                    className="bg-slate-800 hover:bg-slate-700 active:bg-slate-800 text-indigo-300 font-medium text-xs px-3.5 py-2 rounded-xl flex items-center gap-1.5 border border-slate-700 shadow-md transition-all hover:scale-105 active:scale-95"
                  >
                    <Camera className="w-3.5 h-3.5 text-indigo-400" />
                    <span>Take Photo</span>
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Search Box */}
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
            <input 
              type="text" 
              placeholder="Search extracted receipts, items, dishes..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="w-full bg-slate-950/60 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
            />
          </div>

          {/* Action Feeds */}
          <div className="space-y-3">
            {filteredCards.map(card => (
              <div key={card.id} className="bg-slate-800/70 border border-slate-700/60 rounded-2xl p-3.5 space-y-3 shadow-lg hover:border-slate-600 transition-colors">
                
                {/* Header Badge */}
                <div className="flex justify-between items-center">
                  <span className={`text-[10px] font-bold tracking-wider px-2.5 py-1 rounded-md border ${
                    card.detected_category === 'BILL_RECEIPT' ? 'bg-rose-950/60 text-rose-300 border-rose-800/50' :
                    card.detected_category === 'GROCERY_LIST' ? 'bg-emerald-950/60 text-emerald-300 border-emerald-800/50' :
                    card.detected_category === 'FOOD_DISH' ? 'bg-amber-950/60 text-amber-300 border-amber-800/50' :
                    card.detected_category === 'PACKAGED_ITEM' ? 'bg-blue-950/60 text-blue-300 border-blue-800/50' :
                    'bg-slate-950/60 text-slate-300 border-slate-800/50'
                  }`}>
                    {card.detected_category} ({(card.confidence_score * 100).toFixed(0)}%)
                  </span>
                  
                  <button 
                    onClick={() => setEditingCard(card)}
                    className="text-slate-400 hover:text-slate-200 text-xs flex items-center gap-1 bg-slate-900/60 px-2 py-1 rounded-lg border border-slate-700"
                  >
                    <Edit2 className="w-3 h-3" /> Edit
                  </button>
                </div>

                {/* Content Body */}
                <div className="flex gap-3">
                  <img 
                    src={card.imageUri} 
                    alt="Reference" 
                    className="w-18 h-18 object-cover rounded-xl border border-slate-700 flex-shrink-0 cursor-pointer"
                    onClick={() => setEditingCard(card)}
                  />

                  <div className="flex-1 space-y-1.5 text-xs">
                    <h4 className="font-bold text-slate-100 text-sm">{card.summary_title}</h4>

                    {card.detected_category === 'BILL_RECEIPT' && card.expense_details.total_amount && (
                      <div>
                        <p className="text-rose-400 font-extrabold text-base">${card.expense_details.total_amount.toFixed(2)} {card.expense_details.currency || 'USD'}</p>
                        {card.expense_details.merchant && <p className="text-slate-400 text-[11px]">Merchant: {card.expense_details.merchant}</p>}
                      </div>
                    )}

                    {card.detected_category === 'FOOD_DISH' && card.recipe_details.estimated_ingredients_required.length > 0 && (
                      <div className="space-y-1">
                        <p className="text-amber-400 font-semibold text-[11px]">Inferred Recipe Ingredients:</p>
                        <p className="text-slate-300 text-[11px]">{card.recipe_details.estimated_ingredients_required.join(', ')}</p>
                      </div>
                    )}

                    {card.extracted_items.length > 0 && (
                      <div className="space-y-0.5 mt-1">
                        {card.extracted_items.map((item, idx) => (
                          <div key={idx} className="flex justify-between text-slate-300 text-[11px]">
                            <span>• {item.name} {item.quantity ? `(${item.quantity})` : ''}</span>
                            {item.estimated_price && <span className="font-semibold text-emerald-400">${item.estimated_price.toFixed(2)}</span>}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Bottom Navigation Tabs */}
        <div className="bg-slate-950 border-t border-slate-800 px-3 py-2.5 flex justify-around items-center text-[10px]">
          <button onClick={() => setActiveTab('ALL')} className={`flex flex-col items-center gap-1 ${activeTab === 'ALL' ? 'text-indigo-400 font-bold' : 'text-slate-400'}`}>
            <Sparkles className="w-4 h-4" />
            <span>All</span>
          </button>
          <button onClick={() => setActiveTab('BILL_RECEIPT')} className={`flex flex-col items-center gap-1 ${activeTab === 'BILL_RECEIPT' ? 'text-rose-400 font-bold' : 'text-slate-400'}`}>
            <Receipt className="w-4 h-4" />
            <span>Receipts</span>
          </button>
          <button onClick={() => setActiveTab('GROCERY_LIST')} className={`flex flex-col items-center gap-1 ${activeTab === 'GROCERY_LIST' ? 'text-emerald-400 font-bold' : 'text-slate-400'}`}>
            <ShoppingCart className="w-4 h-4" />
            <span>Grocery</span>
          </button>
          <button onClick={() => setActiveTab('FOOD_DISH')} className={`flex flex-col items-center gap-1 ${activeTab === 'FOOD_DISH' ? 'text-amber-400 font-bold' : 'text-slate-400'}`}>
            <Utensils className="w-4 h-4" />
            <span>Dishes</span>
          </button>
          <button onClick={() => setActiveTab('PACKAGED_ITEM')} className={`flex flex-col items-center gap-1 ${activeTab === 'PACKAGED_ITEM' ? 'text-blue-400 font-bold' : 'text-slate-400'}`}>
            <Package className="w-4 h-4" />
            <span>Packages</span>
          </button>
        </div>

      </div>

      {/* Edit Modal */}
      {editingCard && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex justify-center items-end sm:items-center p-3">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-lg p-5 space-y-4 max-h-[85vh] overflow-y-auto shadow-2xl">
            <div className="flex justify-between items-center border-b border-slate-800 pb-3">
              <h3 className="font-bold text-base text-slate-100">Verify Strict JSON Action</h3>
              <button onClick={() => setEditingCard(null)} className="text-slate-400 hover:text-slate-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3">
              <label className="text-xs text-slate-400 font-medium">Summary Title</label>
              <input 
                type="text" 
                value={editingCard.summary_title} 
                onChange={e => setEditingCard({ ...editingCard, summary_title: e.target.value })}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
              />
            </div>

            <button 
              onClick={() => {
                setCards(cards.map(c => c.id === editingCard.id ? editingCard : c));
                setEditingCard(null);
                showToast('Action card saved!');
              }}
              className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 rounded-xl text-xs flex justify-center items-center gap-2"
            >
              <Check className="w-4 h-4" /> Save Action Card
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
