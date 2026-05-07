import { globalIgnores } from 'eslint/config'
import { FlatCompat } from '@eslint/eslintrc'
import { defineConfigWithVueTs, vueTsConfigs } from '@vue/eslint-config-typescript'
import pluginVue from 'eslint-plugin-vue'
import skipFormatting from 'eslint-config-prettier/flat'

const compat = new FlatCompat()

export default defineConfigWithVueTs(
  {
    name: 'app/files-to-lint',
    files: ['**/*.{vue,ts,mts,tsx}']
  },
  globalIgnores(['**/dist/**', '**/dist-ssr/**', '**/coverage/**']),
  ...pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,
  skipFormatting,
  ...compat.extends('./types/.eslintrc-auto-import.json'),
  {
    files: ['**/*.{js,mjs,cjs,jsx,ts,mts,tsx,vue}'],
    rules: {
      'vue/script-indent': ['error', 2, { baseIndent: 0, switchCase: 0, ignores: [] }],
      'vue/multi-word-component-names': 'off',
      'comma-dangle': 'error',
      'comma-spacing': 'error',
      'no-unused-vars': 'off',
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_'
        }
      ],
      'no-var': 'error',
      'no-debugger': 'warn',
      'no-console': 'warn',
      'no-alert': 'warn'
    }
  }
)
