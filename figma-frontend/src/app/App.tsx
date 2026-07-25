import { useState } from "react";
import {
  Settings,
  ChevronLeft,
  Trash2,
  RotateCcw,
  Share2,
  ShoppingCart,
  Check,
  X,
  List,
  Pencil,
  History,
  ChevronRight,
  ArrowUpDown,
  AlertCircle,
} from "lucide-react";

// ══════════════════════════════════════════════════════════════
// Types
// ══════════════════════════════════════════════════════════════

type Screen = "home" | "active" | "products" | "summary" | "history" | "settings";

interface CartItem {
  id: number;
  amount: number;
  quantity: number;
  time: Date;
}

interface Purchase {
  id: number;
  startDate: Date;
  endDate: Date;
  items: CartItem[];
  budget: number | null;
}

interface AppConfig {
  decimals: boolean;
  vibration: boolean;
  sound: boolean;
  keepScreen: boolean;
  hideOnLock: boolean;
  confirmOnFinish: boolean;
  theme: "system" | "light" | "dark";
}

// ══════════════════════════════════════════════════════════════
// Utils
// ══════════════════════════════════════════════════════════════

const ars = (n: number): string =>
  "$ " + Math.round(n).toLocaleString("es-AR");

const fmtTime = (d: Date): string =>
  d.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit" });

const fmtDate = (d: Date): string => {
  const s = d.toLocaleDateString("es-AR", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });
  return s.charAt(0).toUpperCase() + s.slice(1);
};

const fmtShortDate = (d: Date): string =>
  d.toLocaleDateString("es-AR", { day: "numeric", month: "short", year: "numeric" });

const fmtDuration = (a: Date, b: Date): string => {
  const mins = Math.max(0, Math.round((b.getTime() - a.getTime()) / 60000));
  if (mins < 1) return "menos de 1 min";
  if (mins < 60) return `${mins} min`;
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return m > 0 ? `${h}h ${m}m` : `${h}h`;
};

const getTotal = (items: CartItem[]): number =>
  items.reduce((s, i) => s + i.amount * i.quantity, 0);

const getCount = (items: CartItem[]): number =>
  items.reduce((s, i) => s + i.quantity, 0);

const pl = (n: number, s: string, p: string): string =>
  `${n} ${n === 1 ? s : p}`;

// ══════════════════════════════════════════════════════════════
// Sample Data
// ══════════════════════════════════════════════════════════════

const pastDate = (days: number, h: number, m: number): Date => {
  const d = new Date();
  d.setDate(d.getDate() - days);
  d.setHours(h, m, 0, 0);
  return d;
};

const HISTORY_SEED: Purchase[] = [
  {
    id: 1,
    startDate: pastDate(1, 10, 15),
    endDate: pastDate(1, 11, 3),
    budget: 80000,
    items: [
      { id: 11, amount: 3500, quantity: 2, time: pastDate(1, 10, 17) },
      { id: 12, amount: 12500, quantity: 1, time: pastDate(1, 10, 22) },
      { id: 13, amount: 6800, quantity: 3, time: pastDate(1, 10, 31) },
      { id: 14, amount: 4200, quantity: 1, time: pastDate(1, 10, 45) },
      { id: 15, amount: 9900, quantity: 1, time: pastDate(1, 10, 58) },
    ],
  },
  {
    id: 2,
    startDate: pastDate(4, 16, 30),
    endDate: pastDate(4, 17, 12),
    budget: 50000,
    items: [
      { id: 21, amount: 7200, quantity: 1, time: pastDate(4, 16, 33) },
      { id: 22, amount: 4500, quantity: 2, time: pastDate(4, 16, 41) },
      { id: 23, amount: 8900, quantity: 1, time: pastDate(4, 16, 55) },
      { id: 24, amount: 3100, quantity: 3, time: pastDate(4, 17, 5) },
    ],
  },
  {
    id: 3,
    startDate: pastDate(7, 9, 0),
    endDate: pastDate(7, 9, 25),
    budget: null,
    items: [
      { id: 31, amount: 15000, quantity: 1, time: pastDate(7, 9, 5) },
      { id: 32, amount: 2800, quantity: 4, time: pastDate(7, 9, 12) },
      { id: 33, amount: 6500, quantity: 1, time: pastDate(7, 9, 20) },
    ],
  },
];

const NUM_KEYS = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "←"] as const;

// ══════════════════════════════════════════════════════════════
// Reusable UI Components
// ══════════════════════════════════════════════════════════════

function Toggle({ on, toggle }: { on: boolean; toggle: () => void }) {
  return (
    <button
      onClick={toggle}
      role="switch"
      aria-checked={on}
      className={`relative w-12 h-7 rounded-full transition-colors duration-200 flex-shrink-0 ${
        on ? "bg-primary" : "bg-muted-foreground/30"
      }`}
    >
      <span
        className={`absolute top-0.5 left-0.5 w-6 h-6 bg-white rounded-full shadow transition-transform duration-200 ${
          on ? "translate-x-5" : "translate-x-0"
        }`}
      />
    </button>
  );
}

function SRow({
  label,
  sub,
  right,
}: {
  label: string;
  sub?: string;
  right: React.ReactNode;
}) {
  return (
    <div className="flex items-center justify-between py-3.5 border-b border-border last:border-0">
      <div className="mr-4 flex-1 min-w-0">
        <p className="text-sm font-semibold text-foreground">{label}</p>
        {sub && (
          <p className="text-xs text-muted-foreground mt-0.5 leading-snug">{sub}</p>
        )}
      </div>
      <div className="flex-shrink-0">{right}</div>
    </div>
  );
}

function StatRow({
  label,
  value,
  highlight,
}: {
  label: string;
  value: string;
  highlight?: "positive" | "negative";
}) {
  return (
    <div className="flex justify-between items-baseline py-2.5 border-b border-border last:border-0">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span
        className={`font-bold text-base ${
          highlight === "positive"
            ? "text-primary"
            : highlight === "negative"
            ? "text-destructive"
            : "text-foreground"
        }`}
      >
        {value}
      </span>
    </div>
  );
}

function SectionLabel({ label }: { label: string }) {
  return (
    <p className="text-[11px] font-bold text-muted-foreground uppercase tracking-widest mb-1.5">
      {label}
    </p>
  );
}

// ══════════════════════════════════════════════════════════════
// Main App
// ══════════════════════════════════════════════════════════════

export default function App() {
  // ── Navigation ──────────────────────────────────────────────
  const [screen, setScreen] = useState<Screen>("home");

  // ── Purchase state ──────────────────────────────────────────
  const [budgetInput, setBudgetInput] = useState("");
  const [budget, setBudget] = useState<number | null>(null);
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [numInput, setNumInput] = useState("");
  const [qty, setQty] = useState(1);
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [lastSummary, setLastSummary] = useState<Purchase | null>(null);

  // ── Modals ─────────────────────────────────────────────────
  const [showQtyModal, setShowQtyModal] = useState(false);
  const [showFinishDlg, setShowFinishDlg] = useState(false);

  // ── History ─────────────────────────────────────────────────
  const [history, setHistory] = useState<Purchase[]>(HISTORY_SEED);
  const [sortNewest, setSortNewest] = useState(true);
  const [histDetail, setHistDetail] = useState<number | null>(null);

  // ── Products list ───────────────────────────────────────────
  const [prodSort, setProdSort] = useState(true);
  const [editId, setEditId] = useState<number | null>(null);
  const [editVal, setEditVal] = useState("");

  // ── Settings ────────────────────────────────────────────────
  const [cfg, setCfg] = useState<AppConfig>({
    decimals: false,
    vibration: true,
    sound: false,
    keepScreen: true,
    hideOnLock: false,
    confirmOnFinish: true,
    theme: "system",
  });

  // ── Derived ─────────────────────────────────────────────────
  const total = getTotal(cartItems);
  const count = getCount(cartItems);
  const available = budget !== null ? budget - total : null;
  const isExceeded = available !== null && available < 0;
  const pctUsed = budget && budget > 0 ? Math.min((total / budget) * 100, 100) : 0;
  const parsedInput = numInput ? parseInt(numInput, 10) || 0 : 0;
  const lastItem = cartItems.length > 0 ? cartItems[cartItems.length - 1] : null;

  // ── Handlers ────────────────────────────────────────────────

  function handleNumKey(key: string) {
    if (key === "←") {
      setNumInput((p) => p.slice(0, -1));
    } else if (key === "00") {
      if (numInput.length > 0 && numInput.length < 7) setNumInput((p) => p + "00");
    } else {
      if (numInput.length < 8) {
        if (numInput === "" && key === "0") return;
        setNumInput((p) => p + key);
      }
    }
  }

  function startPurchase() {
    const raw = budgetInput.replace(/\D/g, "");
    setBudget(raw ? parseInt(raw, 10) : null);
    setCartItems([]);
    setNumInput("");
    setQty(1);
    setStartDate(new Date());
    setBudgetInput("");
    setScreen("active");
  }

  function addToCart(amount: number, quantity: number) {
    if (amount <= 0) return;
    setCartItems((p) => [
      ...p,
      { id: Date.now(), amount, quantity, time: new Date() },
    ]);
    setNumInput("");
    setQty(1);
    setShowQtyModal(false);
  }

  function handleSumar() {
    if (parsedInput > 0) addToCart(parsedInput, 1);
  }

  function handleCantidad() {
    if (parsedInput > 0) {
      setQty(1);
      setShowQtyModal(true);
    }
  }

  function finishPurchase() {
    const now = new Date();
    const p: Purchase = {
      id: Date.now(),
      startDate: startDate ?? new Date(),
      endDate: now,
      items: [...cartItems],
      budget,
    };
    setHistory((prev) => [p, ...prev]);
    setLastSummary(p);
    setCartItems([]);
    setBudget(null);
    setNumInput("");
    setStartDate(null);
    setShowFinishDlg(false);
    setScreen("summary");
  }

  function deleteCartItem(id: number) {
    setCartItems((p) => p.filter((i) => i.id !== id));
  }

  function saveEdit(id: number) {
    const val = parseInt(editVal.replace(/\D/g, ""), 10);
    if (val > 0) {
      setCartItems((p) => p.map((i) => (i.id === id ? { ...i, amount: val } : i)));
    }
    setEditId(null);
    setEditVal("");
  }

  function deleteHistory(id: number) {
    setHistory((p) => p.filter((h) => h.id !== id));
    if (histDetail === id) setHistDetail(null);
  }

  function toggleCfg(key: keyof Omit<AppConfig, "theme">) {
    setCfg((p) => ({ ...p, [key]: !p[key] }));
  }

  async function shareResumen(p: Purchase) {
    const pTotal = getTotal(p.items);
    const pCount = getCount(p.items);
    const pAvail = p.budget ? p.budget - pTotal : null;
    const lines = [
      "🛒 Voy Gastando — Resumen de compra",
      `Fecha: ${fmtDate(p.endDate)}`,
      `Total: ${ars(pTotal)}`,
      `Unidades: ${pl(pCount, "unidad", "unidades")}`,
      p.budget ? `Presupuesto: ${ars(p.budget)}` : null,
      pAvail !== null
        ? pAvail < 0
          ? `Excedido: ${ars(Math.abs(pAvail))}`
          : `Disponible: ${ars(pAvail)}`
        : null,
      `Duración: ${fmtDuration(p.startDate, p.endDate)}`,
    ]
      .filter(Boolean)
      .join("\n");
    try {
      if (navigator.share) await navigator.share({ text: lines });
      else await navigator.clipboard.writeText(lines);
    } catch {
      /* ignore */
    }
  }

  // ── Screen: Home ─────────────────────────────────────────────

  function renderHome() {
    return (
      <div className="flex flex-col min-h-full bg-background">
        <div className="flex flex-col items-center justify-center px-6 pt-10 pb-6">
          <div
            className="w-20 h-20 bg-primary flex items-center justify-center mb-5 shadow-lg"
            style={{ borderRadius: 28 }}
          >
            <ShoppingCart size={40} className="text-white" strokeWidth={2} />
          </div>
          <h1
            className="text-foreground tracking-tight text-center"
            style={{ fontSize: 32, fontWeight: 900 }}
          >
            Voy Gastando
          </h1>
          <p className="text-sm text-muted-foreground text-center mt-2 leading-snug">
            Llevá el control de tu compra en tiempo real
          </p>
        </div>

        <div className="mx-5 bg-card rounded-2xl border border-border p-4 mb-4">
          <p className="text-sm font-bold text-foreground mb-0.5">Presupuesto</p>
          <p className="text-xs text-muted-foreground mb-3">Podés dejarlo vacío.</p>
          <div className="flex items-center bg-background border border-border rounded-xl overflow-hidden">
            <span className="pl-4 pr-2 text-xl font-black text-muted-foreground select-none">$</span>
            <input
              type="number"
              inputMode="numeric"
              value={budgetInput}
              onChange={(e) => setBudgetInput(e.target.value)}
              placeholder="100.000"
              className="flex-1 py-3 pr-4 text-xl font-bold bg-transparent outline-none text-foreground placeholder:text-muted-foreground/40"
            />
          </div>
        </div>

        <div className="mx-5 mb-4">
          <button
            onClick={startPurchase}
            className="w-full h-16 bg-primary text-white rounded-2xl text-xl font-black tracking-wide shadow-md active:scale-[0.98] transition-transform"
          >
            INICIAR COMPRA
          </button>
        </div>

        <div className="flex gap-3 mx-5 mb-8">
          <button
            onClick={() => { setHistDetail(null); setScreen("history"); }}
            className="flex-1 h-12 flex items-center justify-center gap-2 bg-card border border-border rounded-xl text-sm font-semibold text-foreground active:bg-muted transition-colors"
          >
            <History size={17} />
            Historial
          </button>
          <button
            onClick={() => setScreen("settings")}
            className="flex-1 h-12 flex items-center justify-center gap-2 bg-card border border-border rounded-xl text-sm font-semibold text-foreground active:bg-muted transition-colors"
          >
            <Settings size={17} />
            Configuración
          </button>
        </div>
      </div>
    );
  }

  // ── Screen: Active ───────────────────────────────────────────

  function renderActive() {
    return (
      <div className="flex flex-col min-h-full bg-background relative">
        {/* Total block */}
        <div
          className={`px-5 pt-4 pb-4 ${isExceeded ? "bg-red-50" : "bg-secondary"}`}
        >
          <div className="flex items-start justify-between">
            <div className="flex-1 min-w-0">
              <p className="text-[11px] font-bold text-muted-foreground uppercase tracking-widest mb-1">
                Total del carrito
              </p>
              <p
                className={`font-black leading-none tabular-nums ${
                  isExceeded ? "text-destructive" : "text-foreground"
                }`}
                style={{ fontSize: 54 }}
              >
                {ars(total)}
              </p>
            </div>
            <button
              onClick={() => setScreen("settings")}
              className="mt-1 p-2 rounded-xl active:bg-black/5"
            >
              <Settings size={20} className="text-muted-foreground" />
            </button>
          </div>

          {budget !== null && (
            <div className="mt-3 space-y-1.5">
              <div className="h-2.5 bg-black/10 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-500 ${
                    isExceeded ? "bg-destructive" : "bg-primary"
                  }`}
                  style={{ width: `${pctUsed}%` }}
                />
              </div>
              <div className="flex items-center justify-between">
                <p className="text-xs text-muted-foreground">
                  Presupuesto:{" "}
                  <span className="font-bold text-foreground">{ars(budget)}</span>
                </p>
                <p
                  className={`text-xs font-bold ${
                    isExceeded ? "text-destructive" : "text-muted-foreground"
                  }`}
                >
                  {Math.round(pctUsed)}% usado
                </p>
              </div>
              <p
                className={`text-sm font-black ${
                  isExceeded ? "text-destructive" : "text-primary"
                }`}
              >
                {isExceeded
                  ? `⚠ Excedido: ${ars(Math.abs(available!))}`
                  : `Disponible: ${ars(available!)}`}
              </p>
            </div>
          )}

          <p className="mt-2 text-xs font-medium text-muted-foreground">
            {count === 0 ? "Sin productos aún" : pl(count, "producto", "productos")}
          </p>
        </div>

        {/* Input display */}
        <div className="mx-5 mt-3 mb-2 bg-card border border-border rounded-2xl py-3.5 px-5 shadow-sm">
          <p className="text-right text-4xl font-black text-foreground tracking-tight tabular-nums">
            {parsedInput > 0 ? (
              ars(parsedInput)
            ) : (
              <span className="text-muted-foreground/30">$ 0</span>
            )}
          </p>
        </div>

        {/* Numpad */}
        <div className="px-5 mb-3 grid grid-cols-3 gap-2">
          {NUM_KEYS.map((k) => (
            <button
              key={k}
              onClick={() => handleNumKey(k)}
              className={`h-14 rounded-2xl text-2xl font-black transition-transform active:scale-95 ${
                k === "←"
                  ? "bg-muted text-muted-foreground"
                  : "bg-card border border-border text-foreground shadow-sm"
              }`}
            >
              {k}
            </button>
          ))}
        </div>

        {/* CANTIDAD + SUMAR */}
        <div className="flex gap-2 px-5 mb-3">
          <button
            onClick={handleCantidad}
            disabled={parsedInput === 0}
            className="flex-[2] h-14 border-2 border-primary text-primary rounded-2xl font-black text-[15px] disabled:opacity-30 active:scale-[0.98] transition-transform"
          >
            CANTIDAD
          </button>
          <button
            onClick={handleSumar}
            disabled={parsedInput === 0}
            className="flex-[3] h-14 bg-primary text-white rounded-2xl font-black text-xl shadow-md disabled:opacity-30 active:scale-[0.98] transition-transform"
          >
            SUMAR
          </button>
        </div>

        {/* Last item + undo */}
        {lastItem && (
          <div className="mx-5 mb-3 flex items-center justify-between bg-card border border-border rounded-xl px-4 py-2.5">
            <div>
              <p className="text-[11px] text-muted-foreground font-medium">
                Último · {fmtTime(lastItem.time)}
              </p>
              <p className="text-base font-black text-foreground tabular-nums">
                {ars(lastItem.amount)}
                {lastItem.quantity > 1 && (
                  <span className="text-sm font-semibold text-muted-foreground ml-1">
                    × {lastItem.quantity} = {ars(lastItem.amount * lastItem.quantity)}
                  </span>
                )}
              </p>
            </div>
            <button
              onClick={() => setCartItems((p) => p.slice(0, -1))}
              className="flex items-center gap-1.5 text-destructive font-bold text-sm px-3 py-2 rounded-xl bg-red-50 active:scale-95 transition-transform"
            >
              <RotateCcw size={13} />
              DESHACER
            </button>
          </div>
        )}

        {/* Secondary actions */}
        <div className="flex gap-2 px-5 pb-5 mt-auto">
          <button
            onClick={() => setScreen("products")}
            className="flex-1 h-11 flex items-center justify-center gap-1.5 bg-card border border-border rounded-xl text-sm font-semibold text-foreground active:bg-muted"
          >
            <List size={14} />
            Ver productos
          </button>
          <button
            onClick={() =>
              cfg.confirmOnFinish ? setShowFinishDlg(true) : finishPurchase()
            }
            className="flex-1 h-11 flex items-center justify-center gap-1.5 bg-card border border-border rounded-xl text-sm font-semibold text-foreground active:bg-muted"
          >
            <Check size={14} />
            Finalizar
          </button>
        </div>

        {/* ── Quantity Modal ── */}
        {showQtyModal && (
          <div className="absolute inset-0 bg-black/50 flex items-end z-50">
            <div className="w-full bg-background rounded-t-3xl p-5 pb-8 shadow-2xl">
              <div className="flex items-center justify-between mb-5">
                <h2 className="text-xl font-black">Seleccioná cantidad</h2>
                <button
                  onClick={() => setShowQtyModal(false)}
                  className="p-2 rounded-full bg-muted"
                >
                  <X size={18} />
                </button>
              </div>

              <div className="bg-card rounded-xl px-4 py-3 text-center mb-5 border border-border">
                <p className="text-xs text-muted-foreground mb-0.5">Precio unitario</p>
                <p className="text-2xl font-black tabular-nums">{ars(parsedInput)}</p>
              </div>

              <div className="flex items-center justify-center gap-8 mb-5">
                <button
                  onClick={() => setQty((q) => Math.max(1, q - 1))}
                  className="w-14 h-14 rounded-2xl bg-secondary text-primary font-black text-3xl flex items-center justify-center active:scale-95 transition-transform"
                >
                  −
                </button>
                <span className="text-5xl font-black w-16 text-center tabular-nums">
                  {qty}
                </span>
                <button
                  onClick={() => setQty((q) => q + 1)}
                  className="w-14 h-14 rounded-2xl bg-secondary text-primary font-black text-3xl flex items-center justify-center active:scale-95 transition-transform"
                >
                  +
                </button>
              </div>

              <div className="bg-secondary rounded-xl py-3 text-center mb-5">
                <p className="text-xs text-muted-foreground">Subtotal</p>
                <p className="text-3xl font-black text-primary tabular-nums">
                  {ars(parsedInput * qty)}
                </p>
              </div>

              <button
                onClick={() => addToCart(parsedInput, qty)}
                className="w-full h-14 bg-primary text-white rounded-2xl font-black text-lg shadow active:scale-[0.98] transition-transform"
              >
                AGREGAR {qty} PRODUCTO{qty !== 1 ? "S" : ""}
              </button>
            </div>
          </div>
        )}

        {/* ── Finish Dialog ── */}
        {showFinishDlg && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50 px-5">
            <div className="w-full bg-background rounded-3xl p-5 shadow-2xl">
              <h2 className="text-xl font-black text-center mb-1">Finalizar compra</h2>
              <p className="text-sm text-muted-foreground text-center mb-5">
                ¿Estás listo para terminar?
              </p>

              <div className="bg-card border border-border rounded-2xl px-4 py-1 mb-5">
                <StatRow label="Total" value={ars(total)} />
                <StatRow
                  label="Unidades"
                  value={`${pl(count, "unidad", "unidades")}, ${cartItems.length} registros`}
                />
                {budget !== null && (
                  <>
                    <StatRow label="Presupuesto" value={ars(budget)} />
                    <StatRow
                      label={isExceeded ? "Excedido" : "Disponible"}
                      value={ars(Math.abs(available!))}
                      highlight={isExceeded ? "negative" : "positive"}
                    />
                  </>
                )}
                {startDate && (
                  <StatRow
                    label="Duración aprox."
                    value={fmtDuration(startDate, new Date())}
                  />
                )}
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setShowFinishDlg(false)}
                  className="flex-1 h-12 border-2 border-border rounded-2xl font-bold text-foreground active:bg-muted"
                >
                  CANCELAR
                </button>
                <button
                  onClick={finishPurchase}
                  className="flex-[2] h-12 bg-primary text-white rounded-2xl font-black shadow-md active:scale-[0.98] transition-transform"
                >
                  FINALIZAR
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  }

  // ── Screen: Products ─────────────────────────────────────────

  function renderProducts() {
    const sorted = prodSort ? [...cartItems].reverse() : [...cartItems];

    return (
      <div className="flex flex-col min-h-full bg-background">
        <div className="flex items-center gap-3 px-5 py-4 border-b border-border bg-card sticky top-0 z-10">
          <button
            onClick={() => setScreen("active")}
            className="p-2 -ml-2 rounded-xl active:bg-muted"
          >
            <ChevronLeft size={24} />
          </button>
          <div className="flex-1 min-w-0">
            <h1 className="text-lg font-black leading-tight">Productos cargados</h1>
            <p className="text-xs text-muted-foreground">
              {pl(cartItems.length, "registro", "registros")} ·{" "}
              {pl(count, "unidad", "unidades")} · {ars(total)}
            </p>
          </div>
          <button
            onClick={() => setProdSort((p) => !p)}
            className="flex items-center gap-1 text-xs font-bold text-primary px-2.5 py-1.5 rounded-lg bg-secondary flex-shrink-0"
          >
            <ArrowUpDown size={12} />
            {prodSort ? "Recientes" : "Antiguos"}
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">
          {cartItems.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-48 text-center px-8">
              <ShoppingCart
                size={40}
                className="text-muted-foreground/30 mb-3"
              />
              <p className="text-sm font-semibold text-muted-foreground">
                Sin productos aún
              </p>
              <p className="text-xs text-muted-foreground/60 mt-1">
                Volvé al teclado y sumá el primero
              </p>
            </div>
          ) : (
            <ul className="divide-y divide-border">
              {sorted.map((item, idx) => {
                const order = prodSort ? cartItems.length - idx : idx + 1;
                const isEditing = editId === item.id;
                return (
                  <li key={item.id} className="px-5 py-3.5 bg-card">
                    {isEditing ? (
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-muted-foreground w-5 flex-shrink-0">
                          {order}.
                        </span>
                        <div className="flex-1 flex items-center bg-background border-2 border-primary rounded-xl overflow-hidden">
                          <span className="pl-3 pr-1 font-black text-muted-foreground">
                            $
                          </span>
                          <input
                            type="number"
                            value={editVal}
                            onChange={(e) => setEditVal(e.target.value)}
                            autoFocus
                            className="flex-1 py-2 pr-3 font-black bg-transparent outline-none text-lg"
                          />
                        </div>
                        <button
                          onClick={() => saveEdit(item.id)}
                          className="p-2 rounded-xl bg-primary text-white"
                        >
                          <Check size={16} />
                        </button>
                        <button
                          onClick={() => setEditId(null)}
                          className="p-2 rounded-xl bg-muted"
                        >
                          <X size={16} />
                        </button>
                      </div>
                    ) : (
                      <div className="flex items-center gap-2">
                        <span className="text-xs text-muted-foreground w-5 flex-shrink-0">
                          {order}.
                        </span>
                        <div className="flex-1 min-w-0">
                          <p className="font-black text-foreground text-base leading-tight tabular-nums">
                            {ars(item.amount)}
                            {item.quantity > 1 && (
                              <span className="text-sm font-semibold text-muted-foreground">
                                {" "}
                                × {item.quantity} = {ars(item.amount * item.quantity)}
                              </span>
                            )}
                          </p>
                          <p className="text-xs text-muted-foreground mt-0.5">
                            {fmtTime(item.time)}
                          </p>
                        </div>
                        <button
                          onClick={() => {
                            setEditId(item.id);
                            setEditVal(String(item.amount));
                          }}
                          className="p-2.5 rounded-xl bg-muted active:scale-95"
                        >
                          <Pencil size={14} className="text-muted-foreground" />
                        </button>
                        <button
                          onClick={() => deleteCartItem(item.id)}
                          className="p-2.5 rounded-xl bg-red-50 active:scale-95"
                        >
                          <Trash2 size={14} className="text-destructive" />
                        </button>
                      </div>
                    )}
                  </li>
                );
              })}
            </ul>
          )}
        </div>

        <div className="px-5 py-4 border-t border-border bg-card">
          <div className="flex justify-between items-center">
            <span className="text-sm font-semibold text-muted-foreground">Total</span>
            <span className="text-2xl font-black text-foreground tabular-nums">
              {ars(total)}
            </span>
          </div>
        </div>
      </div>
    );
  }

  // ── Screen: Summary ──────────────────────────────────────────

  function renderSummary() {
    const p = lastSummary;
    if (!p)
      return (
        <div className="flex items-center justify-center min-h-full">
          <p className="text-muted-foreground">Sin datos</p>
        </div>
      );

    const pTotal = getTotal(p.items);
    const pCount = getCount(p.items);
    const pAvail = p.budget ? p.budget - pTotal : null;
    const pExceeded = pAvail !== null && pAvail < 0;
    const avgPerRecord =
      p.items.length > 0 ? Math.round(pTotal / p.items.length) : 0;

    return (
      <div className="flex flex-col min-h-full bg-background">
        <div className="px-5 pt-8 pb-6 bg-primary text-white text-center">
          <div className="w-14 h-14 rounded-full bg-white/20 flex items-center justify-center mx-auto mb-3">
            <Check size={28} className="text-white" strokeWidth={3} />
          </div>
          <h1 className="text-2xl font-black">¡Compra finalizada!</h1>
          <p className="text-white/70 text-sm mt-1">{fmtDate(p.endDate)}</p>
        </div>

        <div className="mx-5 -mt-4 bg-card rounded-2xl border border-border shadow-md p-5 mb-4">
          <p className="text-[11px] font-bold text-muted-foreground uppercase tracking-widest mb-1">
            Total gastado
          </p>
          <p className="font-black text-foreground mb-4 tabular-nums" style={{ fontSize: 48 }}>
            {ars(pTotal)}
          </p>

          {p.budget && (
            <div
              className={`flex items-center gap-2 px-3 py-2 rounded-xl mb-4 ${
                pExceeded ? "bg-red-50" : "bg-secondary"
              }`}
            >
              {pExceeded ? (
                <AlertCircle size={16} className="text-destructive flex-shrink-0" />
              ) : (
                <Check size={16} className="text-primary flex-shrink-0" />
              )}
              <span
                className={`text-sm font-bold ${
                  pExceeded ? "text-destructive" : "text-primary"
                }`}
              >
                {pExceeded
                  ? `Excediste el presupuesto por ${ars(Math.abs(pAvail!))}`
                  : `Te sobran ${ars(pAvail!)} del presupuesto`}
              </span>
            </div>
          )}

          <div className="divide-y divide-border">
            <StatRow
              label="Horario"
              value={`${fmtTime(p.startDate)} — ${fmtTime(p.endDate)}`}
            />
            <StatRow label="Duración" value={fmtDuration(p.startDate, p.endDate)} />
            {p.budget && <StatRow label="Presupuesto" value={ars(p.budget)} />}
            {pAvail !== null && (
              <StatRow
                label={pExceeded ? "Excedido" : "Disponible"}
                value={ars(Math.abs(pAvail))}
                highlight={pExceeded ? "negative" : "positive"}
              />
            )}
            <StatRow
              label="Unidades totales"
              value={pl(pCount, "unidad", "unidades")}
            />
            <StatRow label="Registros" value={String(p.items.length)} />
            <StatRow label="Promedio por registro" value={ars(avgPerRecord)} />
          </div>
        </div>

        <div className="px-5 pb-8 space-y-2">
          <button
            onClick={() => setScreen("home")}
            className="w-full h-14 bg-primary text-white rounded-2xl font-black text-lg shadow active:scale-[0.98] transition-transform"
          >
            NUEVA COMPRA
          </button>
          <div className="flex gap-2">
            <button
              onClick={() => {
                setHistDetail(p.id);
                setScreen("history");
              }}
              className="flex-1 h-12 flex items-center justify-center gap-1.5 bg-card border border-border rounded-2xl font-semibold text-sm active:bg-muted"
            >
              <List size={14} />
              Ver detalle
            </button>
            <button
              onClick={() => shareResumen(p)}
              className="flex-1 h-12 flex items-center justify-center gap-1.5 bg-card border border-border rounded-2xl font-semibold text-sm active:bg-muted"
            >
              <Share2 size={14} />
              Compartir
            </button>
          </div>
          <button
            onClick={() => setScreen("home")}
            className="w-full h-11 flex items-center justify-center font-semibold text-sm text-muted-foreground active:bg-muted rounded-2xl"
          >
            Volver al inicio
          </button>
        </div>
      </div>
    );
  }

  // ── Screen: History ──────────────────────────────────────────

  function renderHistory() {
    // Detail view
    if (histDetail !== null) {
      const p = history.find((h) => h.id === histDetail);
      if (!p) {
        setHistDetail(null);
        return null;
      }
      const pTotal = getTotal(p.items);
      const pCount = getCount(p.items);
      const pAvail = p.budget ? p.budget - pTotal : null;
      const pExceeded = pAvail !== null && pAvail < 0;

      return (
        <div className="flex flex-col min-h-full bg-background">
          <div className="flex items-center gap-3 px-5 py-4 border-b border-border bg-card sticky top-0 z-10">
            <button
              onClick={() => setHistDetail(null)}
              className="p-2 -ml-2 rounded-xl active:bg-muted"
            >
              <ChevronLeft size={24} />
            </button>
            <div className="flex-1 min-w-0">
              <h1 className="text-base font-black leading-tight truncate">
                {fmtDate(p.endDate)}
              </h1>
              <p className="text-xs text-muted-foreground">
                {ars(pTotal)} · {fmtDuration(p.startDate, p.endDate)}
              </p>
            </div>
            <button
              onClick={() => shareResumen(p)}
              className="p-2 rounded-xl active:bg-muted"
            >
              <Share2 size={18} className="text-muted-foreground" />
            </button>
          </div>

          <div className="mx-5 my-4 bg-card border border-border rounded-2xl px-4 py-1">
            <StatRow label="Total gastado" value={ars(pTotal)} />
            {p.budget && <StatRow label="Presupuesto" value={ars(p.budget)} />}
            {pAvail !== null && (
              <StatRow
                label={pExceeded ? "Excedido" : "Disponible"}
                value={ars(Math.abs(pAvail))}
                highlight={pExceeded ? "negative" : "positive"}
              />
            )}
            <StatRow label="Unidades" value={pl(pCount, "unidad", "unidades")} />
            <StatRow label="Duración" value={fmtDuration(p.startDate, p.endDate)} />
            <StatRow
              label="Horario"
              value={`${fmtTime(p.startDate)} — ${fmtTime(p.endDate)}`}
            />
          </div>

          <p className="px-5 pt-1 pb-2 text-[11px] font-bold text-muted-foreground uppercase tracking-widest">
            Detalle de productos
          </p>

          <div className="flex-1 overflow-y-auto">
            <ul className="divide-y divide-border">
              {p.items.map((item, idx) => (
                <li
                  key={item.id}
                  className="flex items-center gap-3 px-5 py-3.5 bg-card"
                >
                  <span className="text-xs text-muted-foreground w-5 flex-shrink-0">
                    {idx + 1}.
                  </span>
                  <div className="flex-1 min-w-0">
                    <p className="font-black text-base tabular-nums">
                      {ars(item.amount)}
                      {item.quantity > 1 && (
                        <span className="text-sm font-semibold text-muted-foreground">
                          {" "}
                          × {item.quantity} = {ars(item.amount * item.quantity)}
                        </span>
                      )}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {fmtTime(item.time)}
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </div>
      );
    }

    // List view
    const sorted = sortNewest ? [...history] : [...history].reverse();

    return (
      <div className="flex flex-col min-h-full bg-background">
        <div className="flex items-center gap-3 px-5 py-4 border-b border-border bg-card sticky top-0 z-10">
          <button
            onClick={() => setScreen("home")}
            className="p-2 -ml-2 rounded-xl active:bg-muted"
          >
            <ChevronLeft size={24} />
          </button>
          <h1 className="flex-1 text-lg font-black">Historial</h1>
          <button
            onClick={() => setSortNewest((p) => !p)}
            className="flex items-center gap-1 text-xs font-bold text-primary px-2.5 py-1.5 rounded-lg bg-secondary flex-shrink-0"
          >
            <ArrowUpDown size={12} />
            {sortNewest ? "Recientes" : "Antiguos"}
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">
          {history.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-48 text-center px-8">
              <History size={40} className="text-muted-foreground/30 mb-3" />
              <p className="text-sm font-semibold text-muted-foreground">
                Sin compras registradas
              </p>
            </div>
          ) : (
            <ul className="divide-y divide-border">
              {sorted.map((p) => {
                const pTotal = getTotal(p.items);
                const pCount = getCount(p.items);
                const pAvail = p.budget ? p.budget - pTotal : null;
                const pExceeded = pAvail !== null && pAvail < 0;
                return (
                  <li key={p.id} className="bg-card px-5 py-4">
                    <div className="flex items-start justify-between mb-1.5">
                      <div className="flex-1 min-w-0 mr-3">
                        <p className="font-bold text-sm text-foreground leading-tight">
                          {fmtShortDate(p.endDate)}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {fmtTime(p.startDate)} — {fmtTime(p.endDate)} ·{" "}
                          {fmtDuration(p.startDate, p.endDate)}
                        </p>
                      </div>
                      <p className="text-xl font-black text-foreground tabular-nums">
                        {ars(pTotal)}
                      </p>
                    </div>
                    <div className="flex items-center gap-2 text-xs text-muted-foreground mb-3">
                      <span>{pl(pCount, "unidad", "unidades")}</span>
                      {p.budget && pAvail !== null && (
                        <>
                          <span>·</span>
                          <span
                            className={
                              pExceeded
                                ? "text-destructive font-semibold"
                                : "text-primary font-semibold"
                            }
                          >
                            {pExceeded
                              ? `Excedido ${ars(Math.abs(pAvail))}`
                              : `Sobran ${ars(pAvail)}`}
                          </span>
                        </>
                      )}
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => setHistDetail(p.id)}
                        className="flex-1 h-9 flex items-center justify-center gap-1.5 bg-secondary text-primary rounded-xl text-xs font-bold active:opacity-80"
                      >
                        <List size={12} />
                        Ver detalle
                      </button>
                      <button
                        onClick={() => deleteHistory(p.id)}
                        className="h-9 px-4 flex items-center justify-center gap-1.5 bg-red-50 text-destructive rounded-xl text-xs font-bold active:opacity-80"
                      >
                        <Trash2 size={12} />
                        Eliminar
                      </button>
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </div>

        {history.length > 0 && (
          <div className="px-5 py-4 border-t border-border bg-card">
            <button
              onClick={() => setHistory([])}
              className="w-full h-11 flex items-center justify-center gap-2 text-destructive font-bold text-sm border border-destructive/30 rounded-xl bg-red-50 active:opacity-80"
            >
              <Trash2 size={14} />
              Borrar historial completo
            </button>
          </div>
        )}
      </div>
    );
  }

  // ── Screen: Settings ─────────────────────────────────────────

  function renderSettings() {
    return (
      <div className="flex flex-col min-h-full bg-background">
        <div className="flex items-center gap-3 px-5 py-4 border-b border-border bg-card sticky top-0 z-10">
          <button
            onClick={() => setScreen(cartItems.length > 0 ? "active" : "home")}
            className="p-2 -ml-2 rounded-xl active:bg-muted"
          >
            <ChevronLeft size={24} />
          </button>
          <h1 className="flex-1 text-lg font-black">Configuración</h1>
        </div>

        <div className="flex-1 overflow-y-auto pb-8">
          <div className="px-5 pt-5 pb-2">
            <SectionLabel label="Moneda" />
            <div className="bg-card border border-border rounded-2xl px-4 py-1">
              <SRow
                label="Moneda"
                sub="Pesos argentinos (ARS)"
                right={
                  <span className="text-sm font-bold text-muted-foreground">$ ARS</span>
                }
              />
              <SRow
                label="Centavos"
                sub="Mostrar decimales en los importes"
                right={
                  <Toggle
                    on={cfg.decimals}
                    toggle={() => toggleCfg("decimals")}
                  />
                }
              />
            </div>
          </div>

          <div className="px-5 pt-4 pb-2">
            <SectionLabel label="Comportamiento" />
            <div className="bg-card border border-border rounded-2xl px-4 py-1">
              <SRow
                label="Vibración al agregar"
                sub="Vibra brevemente cuando sumás un producto"
                right={
                  <Toggle
                    on={cfg.vibration}
                    toggle={() => toggleCfg("vibration")}
                  />
                }
              />
              <SRow
                label="Sonido al agregar"
                sub="Sonido sutil al sumar un importe"
                right={
                  <Toggle on={cfg.sound} toggle={() => toggleCfg("sound")} />
                }
              />
              <SRow
                label="Pantalla encendida"
                sub="Mantener la pantalla activa durante una compra"
                right={
                  <Toggle
                    on={cfg.keepScreen}
                    toggle={() => toggleCfg("keepScreen")}
                  />
                }
              />
              <SRow
                label="Confirmar al finalizar"
                sub="Mostrar diálogo antes de cerrar la compra"
                right={
                  <Toggle
                    on={cfg.confirmOnFinish}
                    toggle={() => toggleCfg("confirmOnFinish")}
                  />
                }
              />
            </div>
          </div>

          <div className="px-5 pt-4 pb-2">
            <SectionLabel label="Privacidad" />
            <div className="bg-card border border-border rounded-2xl px-4 py-1">
              <SRow
                label="Ocultar importes"
                sub="En pantalla de bloqueo y vista de apps recientes"
                right={
                  <Toggle
                    on={cfg.hideOnLock}
                    toggle={() => toggleCfg("hideOnLock")}
                  />
                }
              />
            </div>
          </div>

          <div className="px-5 pt-4 pb-2">
            <SectionLabel label="Apariencia" />
            <div className="bg-card border border-border rounded-2xl px-4 py-1">
              <SRow
                label="Tema"
                right={
                  <div className="flex gap-1">
                    {(["system", "light", "dark"] as const).map((t) => (
                      <button
                        key={t}
                        onClick={() => setCfg((p) => ({ ...p, theme: t }))}
                        className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-colors ${
                          cfg.theme === t
                            ? "bg-primary text-white"
                            : "bg-muted text-muted-foreground"
                        }`}
                      >
                        {t === "system" ? "Auto" : t === "light" ? "Claro" : "Oscuro"}
                      </button>
                    ))}
                  </div>
                }
              />
            </div>
          </div>

          <div className="px-5 pt-4">
            <SectionLabel label="Información" />
            <div className="bg-card border border-border rounded-2xl px-4 py-1">
              <SRow
                label="Almacenamiento local"
                sub="Todos los datos se guardan en tu dispositivo. No usamos servidores."
                right={<ChevronRight size={16} className="text-muted-foreground" />}
              />
              <SRow
                label="Privacidad"
                sub="No compartimos ningún dato con terceros"
                right={<ChevronRight size={16} className="text-muted-foreground" />}
              />
              <SRow
                label="Versión"
                right={
                  <span className="text-xs text-muted-foreground font-semibold">
                    1.0.0
                  </span>
                }
              />
            </div>
          </div>
        </div>
      </div>
    );
  }

  // ── Root render ──────────────────────────────────────────────

  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{ background: "#1C1C1E", fontFamily: "'Nunito', sans-serif" }}
    >
      {/* Phone shell */}
      <div
        className="relative bg-background overflow-hidden"
        style={{
          width: 390,
          minHeight: 844,
          borderRadius: 52,
          boxShadow:
            "0 0 0 10px #2C2C2E, 0 0 0 11px #3A3A3C, 0 40px 100px rgba(0,0,0,0.8)",
          fontFamily: "'Nunito', sans-serif",
        }}
      >
        {/* Status bar */}
        <div className="absolute top-0 left-0 right-0 h-12 flex items-center justify-between px-7 z-20 pointer-events-none">
          <span
            className="text-[13px] text-foreground"
            style={{ fontWeight: 700 }}
          >
            9:41
          </span>
          {/* Dynamic island */}
          <div
            className="absolute left-1/2 -translate-x-1/2 top-2 bg-black"
            style={{ width: 120, height: 34, borderRadius: 20 }}
          />
          <div className="flex items-center gap-1.5">
            {/* Signal bars */}
            <svg
              width="17"
              height="12"
              viewBox="0 0 17 12"
              className="fill-foreground"
            >
              <rect x="0" y="8" width="3" height="4" rx="0.5" />
              <rect x="4.5" y="5" width="3" height="7" rx="0.5" />
              <rect x="9" y="2" width="3" height="10" rx="0.5" />
              <rect x="13.5" y="0" width="3" height="12" rx="0.5" />
            </svg>
            {/* Wifi */}
            <svg
              width="16"
              height="12"
              viewBox="0 0 16 12"
              className="fill-foreground"
            >
              <path d="M8 9.5a1.5 1.5 0 1 1 0 3 1.5 1.5 0 0 1 0-3Z" />
              <path d="M3.5 5.5a6.5 6.5 0 0 1 9 0l-1.5 1.5a4.5 4.5 0 0 0-6 0L3.5 5.5Z" />
              <path d="M0.5 2.5a11 11 0 0 1 15 0L14 4a9 9 0 0 0-12 0L0.5 2.5Z" />
            </svg>
            {/* Battery */}
            <div className="flex items-center gap-0.5">
              <div
                className="border border-foreground/70 rounded-sm overflow-hidden flex"
                style={{ width: 22, height: 11 }}
              >
                <div
                  className="bg-foreground rounded-sm m-0.5"
                  style={{ width: "75%" }}
                />
              </div>
              <div
                className="bg-foreground/70 rounded-full"
                style={{ width: 2, height: 5 }}
              />
            </div>
          </div>
        </div>

        {/* Screen content */}
        <div className="absolute inset-0 top-12 bottom-6 overflow-y-auto overflow-x-hidden">
          {screen === "home" && renderHome()}
          {screen === "active" && renderActive()}
          {screen === "products" && renderProducts()}
          {screen === "summary" && renderSummary()}
          {screen === "history" && renderHistory()}
          {screen === "settings" && renderSettings()}
        </div>

        {/* Home indicator */}
        <div className="absolute bottom-2 left-0 right-0 flex justify-center pointer-events-none">
          <div
            className="bg-foreground/20 rounded-full"
            style={{ width: 134, height: 5 }}
          />
        </div>
      </div>

      {/* Navigation hint for demo */}
      <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-center">
        <p
          className="text-white/30 text-xs"
          style={{ fontFamily: "'Nunito', sans-serif" }}
        >
          Prototipo interactivo · Voy Gastando
        </p>
      </div>
    </div>
  );
}
