import {defineConfig} from 'vitepress'
import zh from './zh.mts'
import en from './en.mts'

export default defineConfig({
    base: '/neutron/',
    rewrites: {
        'zh/:rest*': ':rest*'
    },
    title: 'Neutron',
    description: 'Neutron IAM',
    cleanUrls: true,
    metaChunk: true,
    locales: {
        root: {label: '简体中文', ...zh},
        en: {label: 'English', ...en}
    },
    themeConfig: {
        socialLinks: [
            {icon: 'github', link: 'https://github.com/hejun/neutron'}
        ],
    }
})