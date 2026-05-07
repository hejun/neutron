import postcssPresetEnv from 'postcss-preset-env'
import cssnano from 'cssnano'
import tailwindcssPostcss from '@tailwindcss/postcss'
import autoprefixer from 'autoprefixer'

/** @type {import('postcss-load-config').Config} */
const config = {
  plugins: [
    postcssPresetEnv({ features: {} }),
    tailwindcssPostcss(),
    autoprefixer(),
    process.env.NODE_ENV === 'production' ? cssnano({ preset: 'default' }) : void 0
  ]
}

export default config
