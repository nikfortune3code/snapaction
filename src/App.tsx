import React, { useState, useRef } from 'react';
import { 
  Sparkles, Calendar, ShoppingCart, Receipt, Bookmark, 
  Upload, Plus, Edit2, Trash2, CheckCircle2, AlertCircle, 
  Copy, Download, ExternalLink, Search, Moon, Sun, X, Check, Share2, Camera
} from 'lucide-react';

interface EventDetails {
  title: string;
  startDate: string;
  startTime: string;
  endDate?: string;
  endTime?: string;
  location: string;
  details: string;
}

interface GroceryItem {
  id: string;
  name: string;
  quantity: string;
  checked: boolean;
}

interface GroceryDetails {
  dishName: string;
  items: GroceryItem[];
}

interface ExpenseDetails {
  vendor: string;
  totalAmount: number;
  currency: string;
  dueDate: string;
  category: string;
  isPaid: boolean;
}

interface BookmarkDetails {
  headline: string;
  summary: string;
  keyTakeaways: string[];
  sourcePlatform: string;
}

interface SnapActionCard {
  id: string;
  category: 'EVENT' | 'GROCERY' | 'EXPENSE' | 'BOOKMARK';
  confidenceScore: number;
  imageUri: string;
  timestamp: string;
  event?: EventDetails;
  grocery?: GroceryDetails;
  expense?: ExpenseDetails;
  bookmark?: BookmarkDetails;
}

const INITIAL_DEMO_DATA: SnapActionCard[] = [
  {
    id: 'demo-1',
    category: 'EVENT',
    confidenceScore: 0.98,
    imageUri: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=60',
    timestamp: '2 hours ago',
    event: {
      title: 'Neon Summer Music Festival 2026',
      startDate: '2026-08-24',
      startTime: '18:00',
      location: 'Sunset Amphitheater, Austin TX',
      details: 'Live electronic music festival featuring international DJs. Gates open at 5:00 PM.'
    }
  },
  {
    id: 'demo-2',
    category: 'GROCERY',
    confidenceScore: 0.95,
    imageUri: 'https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=800&auto=format&fit=crop&q=60',
    timestamp: '5 hours ago',
    grocery: {
      dishName: 'Creamy Tuscan Garlic Chicken',
      items: [
        { id: 'g1', name: 'Boneless Chicken Breasts', quantity: '2 lbs', checked: true },
        { id: 'g2', name: 'Heavy Whip Cream', quantity: '1 cup', checked: false },
        { id: 'g3', name: 'Sun-dried Tomatoes in oil', quantity: '1/2 cup', checked: false },
        { id: 'g4', name: 'Fresh Baby Spinach', quantity: '2 cups', checked: true },
        { id: 'g5', name: 'Garlic Cloves', quantity: '4 minced', checked: false },
        { id: 'g6', name: 'Grated Parmesan Cheese', quantity: '1/2 cup', checked: false }
      ]
    }
  },
  {
    id: 'demo-3',
    category: 'EXPENSE',
    confidenceScore: 0.99,
    imageUri: 'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=800&auto=format&fit=crop&q=60',
    timestamp: '12 hours ago',
    expense: {
      vendor: 'Metro Electric Utility Corp',
      totalAmount: 84.50,
      currency: 'USD',
      dueDate: '2026-08-15',
      category: 'Utilities',
      isPaid: false
    }
  }
];

export default function App() {
  const [cards, setCards] = useState<SnapActionCard[]>(INITIAL_DEMO_DATA);
  const [activeTab, setActiveTab] = useState<'ALL' | 'EVENT' | 'GROCERY' | 'EXPENSE'>('ALL');
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

    let category: 'EVENT' | 'GROCERY' | 'EXPENSE' | 'BOOKMARK' = 'GROCERY';
    if (fileName.includes('bill') || fileName.includes('receipt') || fileName.includes('invoice') || fileName.includes('expense') || fileName.includes('pay')) {
      category = 'EXPENSE';
    } else if (fileName.includes('event') || fileName.includes('ticket') || fileName.includes('flyer') || fileName.includes('party') || fileName.includes('concert')) {
      category = 'EVENT';
    } else if (fileName.includes('note') || fileName.includes('article') || fileName.includes('book') || fileName.includes('quote')) {
      category = 'BOOKMARK';
    }

    const cleanTitle = file.name.replace(/\.[^/.]+$/, "").replace(/[-_]/g, " ");

    setProcessingStep('Analyzing image structure & OCR text...');
    setTimeout(() => {
      setProcessingStep(`Categorizing intent (${category})...`);
      setTimeout(() => {
        setProcessingStep('Extracting action items with Gemini AI...');
        setTimeout(() => {
          let newCard: SnapActionCard;

          if (category === 'EXPENSE') {
            newCard = {
              id: 'new-' + Date.now(),
              category: 'EXPENSE',
              confidenceScore: 0.97,
              imageUri: imageUrl,
              timestamp: 'Just now',
              expense: {
                vendor: cleanTitle.length > 3 ? cleanTitle : 'Scanned Receipt/Bill',
                totalAmount: 49.99,
                currency: 'USD',
                dueDate: new Date().toISOString().split('T')[0],
                category: 'Utilities',
                isPaid: false
              }
            };
          } else if (category === 'EVENT') {
            newCard = {
              id: 'new-' + Date.now(),
              category: 'EVENT',
              confidenceScore: 0.98,
              imageUri: imageUrl,
              timestamp: 'Just now',
              event: {
                title: cleanTitle.length > 3 ? cleanTitle : 'Scanned Event Flyer',
                startDate: new Date().toISOString().split('T')[0],
                startTime: '19:00',
                location: 'Main Venue / Location',
                details: 'Action item details extracted from your screenshot.'
              }
            };
          } else if (category === 'BOOKMARK') {
            newCard = {
              id: 'new-' + Date.now(),
              category: 'BOOKMARK',
              confidenceScore: 0.95,
              imageUri: imageUrl,
              timestamp: 'Just now',
              bookmark: {
                headline: cleanTitle.length > 3 ? cleanTitle : 'Saved Screenshot Notes',
                summary: 'Key summary extracted from uploaded screenshot content.',
                keyTakeaways: ['Extracted main concept from screenshot', 'Action item saved'],
                sourcePlatform: 'Uploaded Image'
              }
            };
          } else {
            newCard = {
              id: 'new-' + Date.now(),
              category: 'GROCERY',
              confidenceScore: 0.96,
              imageUri: imageUrl,
              timestamp: 'Just now',
              grocery: {
                dishName: cleanTitle.length > 3 ? cleanTitle : 'Uploaded Recipe / Grocery List',
                items: [
                  { id: 'i1', name: 'Item 1 from screenshot', quantity: '1 unit', checked: false },
                  { id: 'i2', name: 'Item 2 from screenshot', quantity: '2 units', checked: false },
                  { id: 'i3', name: 'Item 3 from screenshot', quantity: 'To taste', checked: false }
                ]
              }
            };
          }

          setCards([newCard, ...cards]);
          setProcessingStep(null);
          showToast('Selected image parsed into Action Card!');
        }, 800);
      }, 700);
    }, 600);
  };

  const toggleGroceryItem = (cardId: string, itemId: string) => {
    setCards(cards.map(c => {
      if (c.id === cardId && c.grocery) {
        const items = c.grocery.items.map(i => i.id === itemId ? { ...i, checked: !i.checked } : i);
        return { ...c, grocery: { ...c.grocery, items } };
      }
      return c;
    }));
  };

  const toggleExpensePaid = (cardId: string) => {
    setCards(cards.map(c => {
      if (c.id === cardId && c.expense) {
        return { ...c, expense: { ...c.expense, isPaid: !c.expense.isPaid } };
      }
      return c;
    }));
  };

  const filteredCards = cards.filter(card => {
    const matchesTab = activeTab === 'ALL' || card.category === activeTab;
    const title = card.event?.title || card.grocery?.dishName || card.expense?.vendor || card.bookmark?.headline || '';
    const matchesSearch = title.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesTab && matchesSearch;
  });

  const totalUnpaid = cards
    .filter(c => c.expense && !c.expense.isPaid)
    .reduce((sum, c) => sum + (c.expense?.totalAmount || 0), 0);

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
              <p className="text-[10px] text-indigo-400 font-medium uppercase tracking-wider">Vision Engine</p>
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
                <h3 className="font-semibold text-sm text-slate-200">Upload, Drop or Capture Screenshot</h3>
                <p className="text-xs text-slate-400 mt-1">Tap to browse files, take photo, or drop receipt/flyer</p>
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
                    <span>Select Screenshot</span>
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
              placeholder="Search actions, events, recipes..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="w-full bg-slate-950/60 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
            />
          </div>

          {/* Expense Banner if active tab is Expense */}
          {activeTab === 'EXPENSE' && (
            <div className="bg-rose-950/40 border border-rose-800/50 rounded-xl p-3 flex justify-between items-center text-xs">
              <span className="font-semibold text-slate-300">Total Unpaid Bills:</span>
              <span className="font-bold text-rose-400 text-sm">${totalUnpaid.toFixed(2)} USD</span>
            </div>
          )}

          {/* Action Feeds */}
          <div className="space-y-3">
            {filteredCards.map(card => (
              <div key={card.id} className="bg-slate-800/70 border border-slate-700/60 rounded-2xl p-3.5 space-y-3 shadow-lg hover:border-slate-600 transition-colors">
                
                {/* Header Badge */}
                <div className="flex justify-between items-center">
                  <span className={`text-[10px] font-bold tracking-wider px-2.5 py-1 rounded-md border ${
                    card.category === 'EVENT' ? 'bg-violet-950/60 text-violet-300 border-violet-800/50' :
                    card.category === 'GROCERY' ? 'bg-emerald-950/60 text-emerald-300 border-emerald-800/50' :
                    card.category === 'EXPENSE' ? 'bg-rose-950/60 text-rose-300 border-rose-800/50' :
                    'bg-blue-950/60 text-blue-300 border-blue-800/50'
                  }`}>
                    {card.category}
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
                    alt="Screenshot" 
                    className="w-18 h-18 object-cover rounded-xl border border-slate-700 flex-shrink-0 cursor-pointer"
                    onClick={() => setEditingCard(card)}
                  />

                  <div className="flex-1 space-y-1.5 text-xs">
                    {/* EVENT */}
                    {card.category === 'EVENT' && card.event && (
                      <div>
                        <h4 className="font-bold text-slate-100 text-sm">{card.event.title}</h4>
                        <p className="text-slate-400 flex items-center gap-1 mt-0.5">
                          <Calendar className="w-3 h-3 text-indigo-400" />
                          {card.event.startDate} • {card.event.startTime}
                        </p>
                        <div className="flex gap-2 mt-2">
                          <a 
                            href={`https://calendar.google.com/calendar/render?action=TEMPLATE&text=${encodeURIComponent(card.event.title)}&location=${encodeURIComponent(card.event.location)}`}
                            target="_blank"
                            rel="noreferrer"
                            className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold px-2.5 py-1 rounded-lg flex items-center gap-1 text-[11px]"
                          >
                            <ExternalLink className="w-3 h-3" /> Sync GCal
                          </a>
                          <button 
                            onClick={() => showToast(`Exported .ics for ${card.event?.title}`)}
                            className="border border-slate-600 text-slate-300 hover:bg-slate-700 px-2.5 py-1 rounded-lg flex items-center gap-1 text-[11px]"
                          >
                            <Download className="w-3 h-3" /> .ics
                          </button>
                        </div>
                      </div>
                    )}

                    {/* GROCERY */}
                    {card.category === 'GROCERY' && card.grocery && (
                      <div>
                        <div className="flex justify-between items-center">
                          <h4 className="font-bold text-slate-100 text-sm">{card.grocery.dishName}</h4>
                          <button 
                            onClick={() => {
                              const txt = card.grocery?.dishName + ':\n' + card.grocery?.items.map(i => `- ${i.name} (${i.quantity})`).join('\n');
                              navigator.clipboard.writeText(txt);
                              showToast('Grocery list copied to clipboard!');
                            }}
                            className="text-indigo-400 hover:text-indigo-300"
                          >
                            <Copy className="w-3.5 h-3.5" />
                          </button>
                        </div>
                        
                        <div className="space-y-1 mt-1.5">
                          {card.grocery.items.map(item => (
                            <label key={item.id} className="flex items-center gap-2 cursor-pointer text-slate-300">
                              <input 
                                type="checkbox" 
                                checked={item.checked} 
                                onChange={() => toggleGroceryItem(card.id, item.id)}
                                className="rounded border-slate-700 bg-slate-900 text-emerald-500 focus:ring-emerald-500 w-3.5 h-3.5"
                              />
                              <span className={item.checked ? 'line-through text-slate-500' : ''}>
                                {item.name} <span className="text-slate-500 text-[10px]">({item.quantity})</span>
                              </span>
                            </label>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* EXPENSE */}
                    {card.category === 'EXPENSE' && card.expense && (
                      <div>
                        <h4 className="font-bold text-slate-100 text-sm">{card.expense.vendor}</h4>
                        <p className="text-rose-400 font-extrabold text-base">${card.expense.totalAmount.toFixed(2)} USD</p>
                        <p className="text-slate-400 text-[11px]">Due: {card.expense.dueDate}</p>
                        
                        <button 
                          onClick={() => toggleExpensePaid(card.id)}
                          className={`mt-2 px-3 py-1 rounded-lg font-bold text-[11px] flex items-center gap-1.5 ${
                            card.expense.isPaid 
                              ? 'bg-emerald-950/80 text-emerald-400 border border-emerald-700/60' 
                              : 'bg-rose-950/80 text-rose-400 border border-rose-700/60'
                          }`}
                        >
                          {card.expense.isPaid ? <CheckCircle2 className="w-3.5 h-3.5" /> : <AlertCircle className="w-3.5 h-3.5" />}
                          {card.expense.isPaid ? 'PAID' : 'MARK PAID'}
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Bottom Navigation Tabs */}
        <div className="bg-slate-950 border-t border-slate-800 px-4 py-2.5 flex justify-around items-center">
          <button onClick={() => setActiveTab('ALL')} className={`flex flex-col items-center gap-1 text-[11px] font-medium ${activeTab === 'ALL' ? 'text-indigo-400' : 'text-slate-400'}`}>
            <Sparkles className="w-5 h-5" />
            <span>Feeds</span>
          </button>
          <button onClick={() => setActiveTab('EVENT')} className={`flex flex-col items-center gap-1 text-[11px] font-medium ${activeTab === 'EVENT' ? 'text-indigo-400' : 'text-slate-400'}`}>
            <Calendar className="w-5 h-5" />
            <span>Events</span>
          </button>
          <button onClick={() => setActiveTab('GROCERY')} className={`flex flex-col items-center gap-1 text-[11px] font-medium ${activeTab === 'GROCERY' ? 'text-indigo-400' : 'text-slate-400'}`}>
            <ShoppingCart className="w-5 h-5" />
            <span>Groceries</span>
          </button>
          <button onClick={() => setActiveTab('EXPENSE')} className={`flex flex-col items-center gap-1 text-[11px] font-medium ${activeTab === 'EXPENSE' ? 'text-indigo-400' : 'text-slate-400'}`}>
            <Receipt className="w-5 h-5" />
            <span>Expenses</span>
          </button>
        </div>

      </div>

      {/* Edit & Verification Slide-Over Modal */}
      {editingCard && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex justify-center items-end sm:items-center p-3">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-lg p-5 space-y-4 max-h-[85vh] overflow-y-auto shadow-2xl">
            <div className="flex justify-between items-center border-b border-slate-800 pb-3">
              <h3 className="font-bold text-base text-slate-100">Edit & Verify AI Action</h3>
              <button onClick={() => setEditingCard(null)} className="text-slate-400 hover:text-slate-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3">
              <p className="text-xs font-semibold text-indigo-400">Original Screenshot Reference</p>
              <img src={editingCard.imageUri} alt="Reference" className="w-full h-40 object-cover rounded-xl border border-slate-800" />
            </div>

            <div className="space-y-3">
              {editingCard.event && (
                <>
                  <div>
                    <label className="text-xs text-slate-400 font-medium">Event Title</label>
                    <input 
                      type="text" 
                      value={editingCard.event.title} 
                      onChange={e => setEditingCard({ ...editingCard, event: { ...editingCard.event!, title: e.target.value } })}
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                    />
                  </div>
                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="text-xs text-slate-400 font-medium">Start Date</label>
                      <input 
                        type="text" 
                        value={editingCard.event.startDate} 
                        onChange={e => setEditingCard({ ...editingCard, event: { ...editingCard.event!, startDate: e.target.value } })}
                        className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                      />
                    </div>
                    <div>
                      <label className="text-xs text-slate-400 font-medium">Start Time</label>
                      <input 
                        type="text" 
                        value={editingCard.event.startTime} 
                        onChange={e => setEditingCard({ ...editingCard, event: { ...editingCard.event!, startTime: e.target.value } })}
                        className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                      />
                    </div>
                  </div>
                </>
              )}

              {editingCard.expense && (
                <>
                  <div>
                    <label className="text-xs text-slate-400 font-medium">Biller Vendor</label>
                    <input 
                      type="text" 
                      value={editingCard.expense.vendor} 
                      onChange={e => setEditingCard({ ...editingCard, expense: { ...editingCard.expense!, vendor: e.target.value } })}
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                    />
                  </div>
                  <div>
                    <label className="text-xs text-slate-400 font-medium">Total Amount ($)</label>
                    <input 
                      type="number" 
                      value={editingCard.expense.totalAmount} 
                      onChange={e => setEditingCard({ ...editingCard, expense: { ...editingCard.expense!, totalAmount: parseFloat(e.target.value) || 0 } })}
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                    />
                  </div>
                </>
              )}
            </div>

            <button 
              onClick={() => {
                setCards(cards.map(c => c.id === editingCard.id ? editingCard : c));
                setEditingCard(null);
                showToast('Action card changes saved!');
              }}
              className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 rounded-xl text-xs flex justify-center items-center gap-2"
            >
              <Check className="w-4 h-4" /> Save Verified Action
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
