/// <reference types="vite/client" />

interface ImportMetaEnv {
  VITE_REQUEST_PREFIX: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
