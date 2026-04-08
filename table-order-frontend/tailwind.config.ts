import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./src/**/*.{js,ts,jsx,tsx,mdx}'],
  theme: {
    extend: {
      colors: {
        // ddocdoc Component Library 색상 토큰
        yellow: {
          700: '#FFDB00',
          450: '#FFE43F',
          400: '#FFED49',
          100: '#FFF8B6',
          50: '#FFFBDB',
        },
        coolGray: {
          900: '#2C3744',
          800: '#506073',
          700: '#5F6972',
          500: '#80878F',
          400: '#959BA1',
          300: '#ABAFB4',
          200: '#E5E8EB',
          100: '#EDF0F3',
          50: '#F5F7F9',
          10: '#FBFCFD',
        },
        red: {
          300: '#F55251',
          200: '#F75E5F',
          100: '#FD857C',
          50: '#FFC5C0',
          10: '#FFF0EF',
        },
        blue: {
          300: '#0077D1',
          50: '#E7F6FF',
          10: '#F4FBFF',
        },
        green: {
          300: '#77C700',
          50: '#EDFAD0',
        },
        orange: {
          300: '#FFB444',
          100: '#FFD699',
          50: '#FFEACA',
          10: '#FFF3E0',
        },
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'Apple SD Gothic Neo', 'sans-serif'],
      },
      fontSize: {
        t1: ['30px', { lineHeight: '1.5' }],
        t3: ['22px', { lineHeight: '1.5', fontWeight: '700' }],
        t5: ['17px', { lineHeight: '1.5', fontWeight: '600' }],
        t6: ['15px', { lineHeight: '1.5', fontWeight: '700' }],
        t7: ['13px', { lineHeight: '1.5', fontWeight: '400' }],
      },
      maxWidth: {
        mobile: '600px',
      },
      boxShadow: {
        depth1: '0px 1px 3px rgba(0,0,0,0.2)',
      },
    },
  },
  plugins: [],
};
export default config;
