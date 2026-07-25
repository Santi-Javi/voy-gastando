/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        background: "#f7f3ea",
        foreground: "#16251d",
        primary: "#1f7a4f",
        "primary-foreground": "#ffffff",
        muted: "#efe6d7",
        "muted-foreground": "#6b6155",
        border: "#dccfbd",
      },
      fontFamily: {
        sans: ["Nunito", "Inter", "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [],
};
