/** @type {import('stylelint').Config} */
const config = {
  extends: ['stylelint-config-standard-scss', 'stylelint-config-recess-order', 'stylelint-prettier/recommended'],
  ignoreFiles: ['**/*.js', '**/*.ts', '**/*.jsx', '**/*.tsx'],
  overrides: [
    {
      files: ['**/*.(vue|html)'],
      customSyntax: 'postcss-html'
    },
    {
      files: ['**/*.(scss|sass)'],
      customSyntax: 'postcss-scss'
    }
  ],
  rules: {
    'prettier/prettier': true,
    'custom-property-empty-line-before': [
      'always',
      {
        except: ['after-custom-property', 'first-nested'],
        ignore: ['after-comment', 'inside-single-line-block', 'value']
      }
    ]
  }
}

export default config
