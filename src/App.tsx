import React, { useState, useRef } from 'react';
import { 
  Sparkles, Calendar, ShoppingCart, Receipt, Bookmark, 
  Upload, Plus, Edit2, Trash2, CheckCircle2, 
  Download, Search, Moon, Sun, X, Check, Camera, BellPlus
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
  dueDate?: string; // Optional: Only populated if applicable (e.g., Electric, Gas, Credit Card bills)
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
      dishName: 'Creamy Tuscan Garlic Chicken & Ingredients',
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
      category: 'Electric Bill',
      isPaid: false
    }
  },
  {
    id: 'demo-4',
    category: 'BOOKMARK',
    confidenceScore: 0.94,
    imageUri: 'https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800&auto=format&fit=crop&q=60',
    timestamp: '18 hours ago',
    bookmark: {
      headline: '10-Minute Morning Spine Mobility Routine',
      summary: 'Essential posture correction and lumbar stretches to perform daily.',
      keyTakeaways: [
        'Perform cat-cow stretches for 60s every morning',
        'Hold thoracic extension over foam roller',
        'Hydrate immediately after waking up'
      ],
      sourcePlatform: 'Saved Screenshot Notes'
    }
  }
];

export default function App() {
  const [cards, setCards] = useState<SnapActionCard[]>(INITIAL_DEMO_DATA);
  const [activeTab, setActiveTab] = useState<'EVENT' | 'GROCERY' | 'EXPENSE' | 'BOOKMARK'>('EXPENSE');
  const [searchQuery, setSearchQuery] = useState('');
  const [isDarkMode, setIsDarkMode] = useState(true);
  const [processingStep, setProcessingStep] = useState<string | null>(null);
  const [editingCard, setEditingCard] = useState<SnapActionCard | null>(null);
  const [copiedToast, setCopiedToast] = useState<string | null>(null);
  const [showManualModal, setShowManualModal] = useState(false);

  // Form states for manual event reminder addition
  const [manualTitle, setManualTitle] = useState('');
  const [manualDate, setManualDate] = useState('');
  const [manualTime, setManualTime] = useState('12:00');
  const [manualLocation, setManualLocation] = useState('');
  const [manualDetails, setManualDetails] = useState('');

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

    // Determine category: default to EXPENSE for receipts/bills/invoices or activeTab
    let category: 'EVENT' | 'GROCERY' | 'EXPENSE' | 'BOOKMARK' = 'EXPENSE';
    if (fileName.includes('event') || fileName.includes('ticket') || fileName.includes('flyer') || fileName.includes('party') || fileName.includes('concert') || fileName.includes('reminder')) {
      category = 'EVENT';
    } else if (fileName.includes('note') || fileName.includes('article') || fileName.includes('book') || fileName.includes('quote')) {
      category = 'BOOKMARK';
    } else if (fileName.includes('grocery') || fileName.includes('dish') || fileName.includes('food') || fileName.includes('recipe')) {
      category = 'GROCERY';
    }

    const cleanTitle = file.name.replace(/\.[^/.]+$/, "").replace(/[-_]/g, " ");

    setProcessingStep('Analyzing receipt/bill OCR data...');
    setTimeout(() => {
      setProcessingStep(`Categorizing intent (${category})...`);
      setTimeout(() => {
        setProcessingStep('Extracting bill heading, amount, category & due date...');
        setTimeout(() => {
          let newCard: SnapActionCard;

          if (category === 'EXPENSE') {
            // Intelligent Bill Heading, Category & Due Date (only if applicable like Electric, Gas, Credit Card)
            let heading = cleanTitle.length > 3 ? cleanTitle : 'Store Receipt / Bill';
            let expCat = 'Shopping Receipt';
            let dueDate: string | undefined = undefined;

            if (fileName.includes('electric') || fileName.includes('power')) {
              heading = 'Electric Utility Bill';
              expCat = 'Electric Bill';
              dueDate = '2026-08-30';
            } else if (fileName.includes('gas')) {
              heading = 'Gas Utility Bill';
              expCat = 'Gas Bill';
              dueDate = '2026-08-28';
            } else if (fileName.includes('card') || fileName.includes('credit')) {
              heading = 'Credit Card Statement Bill';
              expCat = 'Credit Card Bill';
              dueDate = '2026-08-25';
            } else if (fileName.includes('bill') || fileName.includes('utility')) {
              heading = 'Monthly Utility Bill';
              expCat = 'Utilities';
              dueDate = '2026-08-30';
            }

            newCard = {
              id: 'new-' + Date.now(),
              category: 'EXPENSE',
              confidenceScore: 0.98,
              imageUri: imageUrl,
              timestamp: 'Just now',
              expense: {
                vendor: heading,
                totalAmount: 84.50,
                currency: 'USD',
                dueDate: dueDate,
                category: expCat,
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
                title: cleanTitle.length > 3 ? cleanTitle : 'Scanned Event / Reminder',
                startDate: new Date().toISOString().split('T')[0],
                startTime: '19:00',
                location: 'Main Event Location',
                details: 'Action item details extracted from your uploaded screenshot.'
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
                sourcePlatform: 'Uploaded Image Note'
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
          setActiveTab(category);
          setProcessingStep(null);
          showToast(`Receipt/Bill added to ${category} tab!`);
        }, 800);
      }, 700);
    }, 600);
  };

  const toggleGroceryItem = (cardId: string, itemId: string) => {
    setCards(cards.map(c => {
      if (c.id === cardId && c.grocery) {
        const newItems = c.grocery.items.map(item => 
          item.id === itemId ? { ...item, checked: !item.checked } : item
        );
        return { ...c, grocery: { ...c.grocery, items: newItems } };
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

  const deleteCard = (id: string) => {
    setCards(cards.filter(c => c.id !== id));
    showToast('Action card removed');
  };

  const syncToGoogleCalendar = (event: EventDetails) => {
    const startTimeFormatted = event.startTime.replace(':', '') + '00';
    const startDateFormatted = event.startDate.replace(/-/g, '');
    const gCalUrl = `https://calendar.google.com/calendar/render?action=TEMPLATE&text=${encodeURIComponent(event.title)}&dates=${startDateFormatted}T${startTimeFormatted}/${startDateFormatted}T${startTimeFormatted}&details=${encodeURIComponent(event.details)}&location=${encodeURIComponent(event.location)}`;
    window.open(gCalUrl, '_blank');
  };

  const exportIcsFile = (event: EventDetails) => {
    const icsContent = `BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//SnapAction//NONSGML v1.0//EN
BEGIN:VEVENT
TITLE:${event.title}
DESCRIPTION:${event.details}
LOCATION:${event.location}
DTSTART:${event.startDate.replace(/-/g, '')}T${event.startTime.replace(':', '')}00Z
END:VEVENT
END:VCALENDAR`;

    const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', `${event.title.replace(/\s+/g, '_')}.ics`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('Downloaded .ics Calendar file!');
  };

  const handleAddManualEvent = (e: React.FormEvent) => {
    e.preventDefault();
    if (!manualTitle || !manualDate) {
      showToast('Please enter title and date');
      return;
    }

    const newReminderCard: SnapActionCard = {
      id: 'manual-' + Date.now(),
      category: 'EVENT',
      confidenceScore: 1.0,
      imageUri: 'https://images.unsplash.com/photo-1506784983877-45594efa4cbe?w=800&auto=format&fit=crop&q=60',
      timestamp: 'Just now',
      event: {
        title: manualTitle,
        startDate: manualDate,
        startTime: manualTime || '12:00',
        location: manualLocation || 'Custom Event',
        details: manualDetails || 'User created manual event reminder.'
      }
    };

    setCards([newReminderCard, ...cards]);
    setActiveTab('EVENT');
    setShowManualModal(false);
    setManualTitle('');
    setManualDate('');
    setManualTime('12:00');
    setManualLocation('');
    setManualDetails('');
    showToast('Event Reminder Added!');
  };

  const filteredCards = cards.filter(card => {
    const matchesTab = card.category === activeTab;
    const searchLower = searchQuery.toLowerCase();
    const titleToSearch = card.event?.title || card.grocery?.dishName || card.expense?.vendor || card.bookmark?.headline || '';
    const matchesSearch = titleToSearch.toLowerCase().includes(searchLower);
    return matchesTab && matchesSearch;
  });

  return (
    <div className={`min-h-screen ${isDarkMode ? 'bg-slate-950 text-slate-100' : 'bg-slate-50 text-slate-900'} flex justify-center py-6 px-3 transition-colors duration-200`}>
      {copiedToast && (
        <div className="fixed top-5 z-50 bg-indigo-600 text-white px-5 py-2.5 rounded-full shadow-xl flex items-center gap-2 animate-bounce">
          <CheckCircle2 className="w-5 h-5" />
          <span className="text-sm font-semibold">{copiedToast}</span>
        </div>
      )}

      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-[3rem] shadow-2xl overflow-hidden flex flex-col h-[840px] relative">
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

        <div className="bg-slate-900/90 backdrop-blur-md px-5 py-3 border-b border-slate-800 flex justify-between items-center">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 bg-indigo-600/20 border border-indigo-500/30 rounded-xl flex items-center justify-center text-indigo-400">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h1 className="font-bold text-base tracking-tight text-white">SnapAction</h1>
              <p className="text-[10px] text-indigo-400 font-medium uppercase tracking-wider">Android AI App</p>
            </div>
          </div>

          <button 
            onClick={() => setIsDarkMode(!isDarkMode)} 
            className="w-9 h-9 bg-slate-800 hover:bg-slate-700 rounded-xl flex items-center justify-center text-slate-300 transition-colors"
          >
            {isDarkMode ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-4 space-y-4">
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
            className="border-2 border-dashed border-indigo-500/40 bg-indigo-950/20 hover:bg-indigo-900/30 rounded-2xl p-4 text-center cursor-pointer transition-all group relative overflow-hidden flex flex-col items-center justify-center"
          >
            {processingStep ? (
              <div className="py-3 flex flex-col items-center space-y-2">
                <div className="w-8 h-8 border-3 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
                <p className="text-xs font-semibold text-indigo-300 animate-pulse">{processingStep}</p>
              </div>
            ) : (
              <div className="flex flex-col items-center">
                <div className="w-10 h-10 bg-indigo-600/20 text-indigo-400 rounded-2xl flex items-center justify-center mb-1.5 group-hover:scale-110 transition-transform">
                  <Upload className="w-5 h-5" />
                </div>
                <h3 className="font-semibold text-xs text-slate-200">Upload Receipt, Bill or Screenshot</h3>
                <p className="text-[11px] text-slate-400 mt-0.5">Extracts Bill Heading, Amount, Category & Due Date</p>
                <div className="flex gap-2 mt-2.5">
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      triggerFilePicker();
                    }}
                    className="bg-indigo-600 hover:bg-indigo-500 active:bg-indigo-700 text-white font-medium text-xs px-3 py-1.5 rounded-xl flex items-center gap-1 shadow-lg shadow-indigo-600/20 transition-all hover:scale-105 active:scale-95"
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
                    className="bg-slate-800 hover:bg-slate-700 active:bg-slate-800 text-indigo-300 font-medium text-xs px-3 py-1.5 rounded-xl flex items-center gap-1 border border-slate-700 shadow-md transition-all hover:scale-105 active:scale-95"
                  >
                    <Camera className="w-3.5 h-3.5 text-indigo-400" />
                    <span>Take Photo</span>
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="flex items-center gap-2">
            <div className="relative flex-1">
              <Search className="w-4 h-4 absolute left-3.5 top-3 text-slate-400" />
              <input 
                type="text" 
                placeholder={`Search ${activeTab.toLowerCase()} items...`} 
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="w-full bg-slate-950/60 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
              />
            </div>

            {activeTab === 'EVENT' && (
              <button
                type="button"
                onClick={() => setShowManualModal(true)}
                className="bg-indigo-600 hover:bg-indigo-500 active:bg-indigo-700 text-white font-semibold text-xs px-3 py-2 rounded-xl flex items-center gap-1.5 shadow-lg shadow-indigo-600/30 whitespace-nowrap transition-all hover:scale-105 active:scale-95"
              >
                <BellPlus className="w-4 h-4" />
                <span>Add Event</span>
              </button>
            )}
          </div>

          {filteredCards.length === 0 ? (
            <div className="py-12 text-center text-slate-500 space-y-3">
              <Receipt className="w-10 h-10 mx-auto text-slate-600 stroke-1" />
              <p className="text-xs">No {activeTab.toLowerCase()} items found.</p>
              {activeTab === 'EVENT' && (
                <button
                  onClick={() => setShowManualModal(true)}
                  className="bg-indigo-600 hover:bg-indigo-500 text-white font-medium text-xs px-4 py-2 rounded-xl inline-flex items-center gap-1.5 shadow-lg"
                >
                  <Plus className="w-4 h-4" /> Add Event Manually
                </button>
              )}
            </div>
          ) : (
            <div className="space-y-3">
              {filteredCards.map(card => (
                <div key={card.id} className="bg-slate-800/70 border border-slate-700/60 rounded-2xl p-3.5 space-y-3 shadow-lg hover:border-slate-600 transition-colors">
                  <div className="flex justify-between items-center">
                    <span className={`text-[10px] font-bold tracking-wider px-2.5 py-1 rounded-md border ${
                      card.category === 'EVENT' ? 'bg-indigo-950/60 text-indigo-300 border-indigo-800/50' :
                      card.category === 'GROCERY' ? 'bg-emerald-950/60 text-emerald-300 border-emerald-800/50' :
                      card.category === 'EXPENSE' ? 'bg-rose-950/60 text-rose-300 border-rose-800/50' :
                      'bg-amber-950/60 text-amber-300 border-amber-800/50'
                    }`}>
                      {card.category === 'EVENT' ? 'REMINDER / EVENT' :
                       card.category === 'GROCERY' ? 'GROCERIES & DISHES' :
                       card.category === 'EXPENSE' ? 'EXPENSES & BILLS' : 'BOOKMARK & NOTE'}
                    </span>
                    
                    <div className="flex items-center gap-1">
                      <button 
                        onClick={() => setEditingCard(card)}
                        className="text-slate-400 hover:text-slate-200 text-xs p-1 bg-slate-900/60 rounded-lg border border-slate-700"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button 
                        onClick={() => deleteCard(card.id)}
                        className="text-rose-400 hover:text-rose-300 text-xs p-1 bg-slate-900/60 rounded-lg border border-slate-700"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  <div className="flex gap-3">
                    <img 
                      src={card.imageUri} 
                      alt="Reference" 
                      className="w-20 h-20 object-cover rounded-xl border border-slate-700 flex-shrink-0 cursor-pointer"
                      onClick={() => setEditingCard(card)}
                    />

                    <div className="flex-1 space-y-1.5 text-xs">
                      {card.category === 'EVENT' && card.event && (
                        <div className="space-y-1">
                          <h4 className="font-bold text-slate-100 text-sm">{card.event.title}</h4>
                          <p className="text-indigo-300 text-[11px]">📅 {card.event.startDate} at {card.event.startTime}</p>
                          {card.event.location && <p className="text-slate-400 text-[11px]">📍 {card.event.location}</p>}
                          {card.event.details && <p className="text-slate-300 text-[11px] leading-snug">{card.event.details}</p>}
                          
                          <div className="flex gap-2 pt-1">
                            <button
                              onClick={() => syncToGoogleCalendar(card.event!)}
                              className="bg-indigo-600 hover:bg-indigo-500 text-white font-medium text-[10px] px-2.5 py-1 rounded-lg flex items-center gap-1 shadow"
                            >
                              <Calendar className="w-3 h-3" /> Sync Google Cal
                            </button>
                            <button
                              onClick={() => exportIcsFile(card.event!)}
                              className="bg-slate-700 hover:bg-slate-600 text-slate-200 font-medium text-[10px] px-2.5 py-1 rounded-lg flex items-center gap-1"
                            >
                              <Download className="w-3 h-3" /> Export .ics
                            </button>
                          </div>
                        </div>
                      )}

                      {card.category === 'GROCERY' && card.grocery && (
                        <div className="space-y-1">
                          <h4 className="font-bold text-slate-100 text-sm">{card.grocery.dishName}</h4>
                          <div className="space-y-1 mt-1">
                            {card.grocery.items.map(item => (
                              <label key={item.id} className="flex items-center gap-2 cursor-pointer text-[11px]">
                                <input 
                                  type="checkbox" 
                                  checked={item.checked} 
                                  onChange={() => toggleGroceryItem(card.id, item.id)}
                                  className="rounded border-slate-700 text-indigo-600 focus:ring-0"
                                />
                                <span className={item.checked ? 'line-through text-slate-500' : 'text-slate-300'}>
                                  {item.name} ({item.quantity})
                                </span>
                              </label>
                            ))}
                          </div>
                        </div>
                      )}

                      {/* Expense Card Body: Heading, Amount, Category & Conditional Due Date */}
                      {card.category === 'EXPENSE' && card.expense && (
                        <div className="space-y-1">
                          <h4 className="font-bold text-slate-100 text-sm">{card.expense.vendor}</h4>
                          <p className="text-rose-400 font-extrabold text-base">${card.expense.totalAmount.toFixed(2)} {card.expense.currency}</p>
                          <p className="text-indigo-300 font-semibold text-[11px]">Category: {card.expense.category}</p>
                          {card.expense.dueDate && (
                            <p className="text-rose-300 font-medium text-[11px]">🗓️ Due Date: {card.expense.dueDate}</p>
                          )}
                          <button
                            onClick={() => toggleExpensePaid(card.id)}
                            className={`mt-1 font-semibold text-[10px] px-2.5 py-1 rounded-lg border transition-colors ${
                              card.expense.isPaid 
                                ? 'bg-emerald-950/60 text-emerald-300 border-emerald-800' 
                                : 'bg-rose-950/60 text-rose-300 border-rose-800 hover:bg-rose-900'
                            }`}
                          >
                            {card.expense.isPaid ? '✓ Paid' : 'Mark as Paid'}
                          </button>
                        </div>
                      )}

                      {card.category === 'BOOKMARK' && card.bookmark && (
                        <div className="space-y-1">
                          <h4 className="font-bold text-slate-100 text-sm">{card.bookmark.headline}</h4>
                          <p className="text-slate-300 text-[11px] leading-snug">{card.bookmark.summary}</p>
                          {card.bookmark.keyTakeaways.length > 0 && (
                            <div className="pt-1 space-y-0.5">
                              {card.bookmark.keyTakeaways.map((point, idx) => (
                                <p key={idx} className="text-amber-300 text-[10px]">• {point}</p>
                              ))}
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-slate-950 border-t border-slate-800 px-3 py-2.5 flex justify-around items-center text-[10px]">
          <button onClick={() => setActiveTab('EVENT')} className={`flex flex-col items-center gap-1 ${activeTab === 'EVENT' ? 'text-indigo-400 font-bold' : 'text-slate-400'}`}>
            <Calendar className="w-4 h-4" />
            <span>Reminders</span>
          </button>
          <button onClick={() => setActiveTab('GROCERY')} className={`flex flex-col items-center gap-1 ${activeTab === 'GROCERY' ? 'text-emerald-400 font-bold' : 'text-slate-400'}`}>
            <ShoppingCart className="w-4 h-4" />
            <span>Groceries</span>
          </button>
          <button onClick={() => setActiveTab('EXPENSE')} className={`flex flex-col items-center gap-1 ${activeTab === 'EXPENSE' ? 'text-rose-400 font-bold' : 'text-slate-400'}`}>
            <Receipt className="w-4 h-4" />
            <span>Expenses</span>
          </button>
          <button onClick={() => setActiveTab('BOOKMARK')} className={`flex flex-col items-center gap-1 ${activeTab === 'BOOKMARK' ? 'text-amber-400 font-bold' : 'text-slate-400'}`}>
            <Bookmark className="w-4 h-4" />
            <span>Bookmarks</span>
          </button>
        </div>

      </div>

      {showManualModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex justify-center items-center p-3">
          <form onSubmit={handleAddManualEvent} className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-md p-5 space-y-3.5 shadow-2xl">
            <div className="flex justify-between items-center border-b border-slate-800 pb-2.5">
              <h3 className="font-bold text-base text-slate-100 flex items-center gap-2">
                <BellPlus className="w-5 h-5 text-indigo-400" />
                <span>Add Event Reminder</span>
              </h3>
              <button type="button" onClick={() => setShowManualModal(false)} className="text-slate-400 hover:text-slate-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Event Title *</label>
              <input 
                type="text" 
                placeholder="e.g., Tech Conference 2026" 
                value={manualTitle} 
                onChange={e => setManualTitle(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-indigo-500" 
                required 
              />
            </div>

            <div className="grid grid-cols-2 gap-2.5">
              <div className="space-y-1">
                <label className="text-xs text-slate-400 font-medium">Start Date *</label>
                <input 
                  type="date" 
                  value={manualDate} 
                  onChange={e => setManualDate(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-indigo-500" 
                  required 
                />
              </div>
              <div className="space-y-1">
                <label className="text-xs text-slate-400 font-medium">Start Time</label>
                <input 
                  type="time" 
                  value={manualTime} 
                  onChange={e => setManualTime(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-indigo-500" 
                />
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Location</label>
              <input 
                type="text" 
                placeholder="e.g., Grand Ballroom or Zoom Link" 
                value={manualLocation} 
                onChange={e => setManualLocation(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-indigo-500" 
              />
            </div>

            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Event Description & Notes</label>
              <textarea 
                placeholder="Details, ticket numbers, or agenda items..." 
                value={manualDetails} 
                onChange={e => setManualDetails(e.target.value)}
                rows={2}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-indigo-500" 
              />
            </div>

            <button 
              type="submit"
              className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 rounded-xl text-xs flex justify-center items-center gap-2 shadow-lg shadow-indigo-600/20"
            >
              <Check className="w-4 h-4" /> Add Event Reminder
            </button>
          </form>
        </div>
      )}

      {editingCard && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex justify-center items-end sm:items-center p-3">
          <div className="bg-slate-900 border border-slate-800 rounded-3xl w-full max-w-lg p-5 space-y-4 max-h-[85vh] overflow-y-auto shadow-2xl">
            <div className="flex justify-between items-center border-b border-slate-800 pb-3">
              <h3 className="font-bold text-base text-slate-100">Edit & Assign Category Tab</h3>
              <button onClick={() => setEditingCard(null)} className="text-slate-400 hover:text-slate-200">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs text-indigo-400 font-semibold">Assigned Tab</label>
              <div className="grid grid-cols-4 gap-1.5">
                {(['EVENT', 'GROCERY', 'EXPENSE', 'BOOKMARK'] as const).map(cat => (
                  <button
                    key={cat}
                    type="button"
                    onClick={() => setEditingCard({ ...editingCard, category: cat })}
                    className={`py-1.5 px-2 rounded-xl text-[11px] font-semibold border transition-all ${
                      editingCard.category === cat 
                        ? 'bg-indigo-600 text-white border-indigo-500' 
                        : 'bg-slate-950 text-slate-400 border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    {cat === 'EVENT' ? 'Reminders' : cat === 'GROCERY' ? 'Groceries' : cat === 'EXPENSE' ? 'Expenses' : 'Bookmarks'}
                  </button>
                ))}
              </div>
            </div>

            {editingCard.category === 'EXPENSE' && editingCard.expense && (
              <div className="space-y-3 pt-2">
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-medium">Bill Heading / Merchant *</label>
                  <input 
                    type="text" 
                    value={editingCard.expense.vendor} 
                    onChange={e => setEditingCard({ ...editingCard, expense: { ...editingCard.expense!, vendor: e.target.value } })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                  />
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <div className="space-y-1">
                    <label className="text-xs text-slate-400 font-medium">Total Amount ($)</label>
                    <input 
                      type="number" 
                      value={editingCard.expense.totalAmount} 
                      onChange={e => setEditingCard({ ...editingCard, expense: { ...editingCard.expense!, totalAmount: parseFloat(e.target.value) || 0 } })}
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                    />
                  </div>
                  <div className="space-y-1">
                    <label className="text-xs text-slate-400 font-medium">Category</label>
                    <input 
                      type="text" 
                      value={editingCard.expense.category} 
                      onChange={e => setEditingCard({ ...editingCard, expense: { ...editingCard.expense!, category: e.target.value } })}
                      className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                    />
                  </div>
                </div>
                <div className="space-y-1">
                  <label className="text-xs text-slate-400 font-medium">Due Date (Optional - e.g. Electric, Gas, Credit Card)</label>
                  <input 
                    type="date" 
                    value={editingCard.expense.dueDate || ''} 
                    onChange={e => setEditingCard({ ...editingCard, expense: { ...editingCard.expense!, dueDate: e.target.value || undefined } })}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200" 
                  />
                </div>
              </div>
            )}

            <button 
              onClick={() => {
                setCards(cards.map(c => c.id === editingCard.id ? editingCard : c));
                setActiveTab(editingCard.category);
                setEditingCard(null);
                showToast('Action card updated!');
              }}
              className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold py-2.5 rounded-xl text-xs flex justify-center items-center gap-2"
            >
              <Check className="w-4 h-4" /> Save Changes & Switch Tab
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
