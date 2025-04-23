import eslint from '@eslint/js'
import globals from 'globals'
import eslintPluginVue from 'eslint-plugin-vue'
import typescriptEslint from 'typescript-eslint'
import eslintConfigPrettier from 'eslint-config-prettier'

export default typescriptEslint.config(
  { ignores: ['**/dist'] },
  {
    extends: [
      eslint.configs.recommended,
      ...typescriptEslint.configs.recommended,
      ...eslintPluginVue.configs['flat/strongly-recommended']
    ],
    files: ['**/*.{js,mjs,cjs,jsx,ts,tsx,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: globals.browser,
      parserOptions: {
        parser: typescriptEslint.parser,
        ecmaFeatures: {
          jsx: true
        }
      }
    },
    rules: {
      'vue/script-indent': ['error', 2, { baseIndent: 0, switchCase: 0, ignores: [] }],
      'vue/multi-word-component-names': 'off',
      'comma-dangle': 'error',
      'comma-spacing': 'error',
      'no-unused-vars': 'error',
      'no-var': 'error',
      'no-debugger': 'warn',
      'no-console': 'warn',
      'no-alert': 'warn'
    }
  },
  eslintConfigPrettier
)
