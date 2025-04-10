/// <reference types="vite/client" />

interface ImportMetaEnv {
  VITE_REQUEST_PREFIX: string
  VITE_CLIENT_ID: string
  VITE_REDIRECT_URI: string
  VITE_SCOPE: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
