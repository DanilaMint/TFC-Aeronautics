const fs = require('fs');
const path = require('path');
const { build } = require('vite');
const { transform } = require('@babel/core');

const SRC_PATH = './src';

// Выходные папки напрямую в корне проекта
const OUTPUT_DIRS = {
    client: './client_scripts',
    server: './server_scripts',
    startup: './startup_scripts'
};

// Создаем все необходимые папки
Object.values(OUTPUT_DIRS).forEach(dir => {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
});

// Babel конфигурация для Rhino
const BABEL_CONFIG = {
    presets: [
        ['@babel/preset-env', {
            targets: { rhino: '1.7.13' },
            modules: false
        }]
    ],
    plugins: [
        '@babel/plugin-transform-block-scoping',
        '@babel/plugin-transform-arrow-functions',
        '@babel/plugin-transform-template-literals',
        '@babel/plugin-transform-classes'
    ]
};

// Получаем список папок
function getFolders(dir) {
    return fs.readdirSync(dir).filter(file => {
        return fs.statSync(path.join(dir, file)).isDirectory();
    });
}

// Создаем виртуальный модуль для каждого типа
function createVirtualModule(type, folders) {
    let imports = '';
    let exports = '';
    let modules = {};

    folders.forEach(folder => {
        const filePath = path.join(SRC_PATH, folder, `${type}.js`);
        if (fs.existsSync(filePath)) {
            const moduleName = `${folder}_${type}`;
            imports += `import * as ${moduleName} from './${filePath}';\n`;
            modules[moduleName] = filePath;
            exports += `Object.assign(exports, ${moduleName});\n`;
        }
    });

    return {
        code: `${imports}\nexport default {\n${Object.keys(modules).map(name => `    ${name},`).join('\n')}\n};\n`,
        modules
    };
}

// Сборка через Vite
async function buildWithVite(type) {
    const folders = getFolders(SRC_PATH);
    const outputDir = OUTPUT_DIRS[type];
    const outputFile = path.join(outputDir, `${type}.js`);

    console.log(`\n📦 Сборка ${type}.js через Vite...`);

    if (!folders.some(folder => fs.existsSync(path.join(SRC_PATH, folder, `${type}.js`)))) {
        console.log(`   ⚠️ Нет файлов типа ${type}.js`);
        return;
    }

    // Создаем временный входной файл
    const virtualModule = createVirtualModule(type, folders);
    const tempFile = path.join(outputDir, `_temp_${type}.js`);
    fs.writeFileSync(tempFile, virtualModule.code, 'utf8');

    try {
        // Собираем через Vite
        await build({
            build: {
                lib: {
                    entry: tempFile,
                    name: type,
                    formats: ['iife'],
                    fileName: () => `${type}.js`
                },
                outDir: outputDir,
                rollupOptions: {
                    external: [],
                    output: {
                        globals: {},
                        name: type,
                        extend: false,
                        esModule: false,
                        compact: false
                    }
                },
                minify: false,
                sourcemap: false,
                target: 'es5'
            },
            configFile: false,
            plugins: [
                {
                    name: 'rhino-transform',
                    transform(code) {
                        try {
                            const result = transform(code, {
                                ...BABEL_CONFIG,
                                filename: `${type}.js`
                            });
                            return result.code;
                        } catch (error) {
                            console.error('   ❌ Babel transform error:', error.message);
                            return code;
                        }
                    }
                }
            ]
        });

        // Читаем сгенерированный файл
        const builtFile = path.join(outputDir, `${type}.js`);
        if (fs.existsSync(builtFile)) {
            let content = fs.readFileSync(builtFile, 'utf8');

            // Добавляем дополнительную изоляцию через IIFE если нужно
            if (!content.includes('(function') && !content.includes('(() =>')) {
                content = `(function() {\n${content}\n})();\n`;
            }

            fs.writeFileSync(builtFile, content, 'utf8');
            console.log(`   ✅ ${type}.js собран в ${builtFile}`);
            console.log(`   📁 Модулей: ${Object.keys(virtualModule.modules).length}`);
        }

    } catch (error) {
        console.error(`   ❌ Ошибка при сборке ${type}.js:`, error.message);
    } finally {
        // Удаляем временный файл
        if (fs.existsSync(tempFile)) {
            fs.unlinkSync(tempFile);
        }
    }
}

// Функция для обработки содержимого модуля
function processModuleContent(content) {
    // Удаляем импорты/экспорты
    content = content.replace(/^export\s+default\s+/gm, '');
    content = content.replace(/^export\s+/gm, '');
    content = content.replace(/^import\s+.*?from\s+['"](.+?)['"];?\s*$/gm, '');
    content = content.replace(/^import\s+['"](.+?)['"];?\s*$/gm, '');

    // Удаляем комментарии, которые могут вызвать проблемы
    content = content.replace(/\/\*[\s\S]*?\*\//g, ''); // Многострочные комментарии
    content = content.replace(/\/\/.*$/gm, ''); // Однострочные комментарии

    return content.trim();
}

// Альтернативный вариант с ручным объединением и IIFE
function buildWithIIFE(type) {
    const folders = getFolders(SRC_PATH);
    const outputDir = OUTPUT_DIRS[type];
    const outputFile = path.join(outputDir, `${type}.js`);
    let modules = {};

    console.log(`\n📦 Сборка ${type}.js через IIFE...`);

    // Собираем все файлы в один модульный объект
    folders.forEach(folder => {
        const filePath = path.join(SRC_PATH, folder, `${type}.js`);
        if (fs.existsSync(filePath)) {
            let content = fs.readFileSync(filePath, 'utf8');
            const moduleName = `${folder}_${type}`;

            // Обрабатываем содержимое модуля
            content = processModuleContent(content);

            modules[moduleName] = content;
        }
    });

    if (Object.keys(modules).length === 0) {
        console.log(`   ⚠️ Нет файлов типа ${type}.js`);
        return;
    }

    // Для разных типов используем разные подходы
    const isStartup = type === 'startup';
    const isClient = type === 'client';
    const isServer = type === 'server';

    // Создаем IIFE с изолированной областью видимости
    let finalContent = `(function() {\n`;
    finalContent += `    'use strict';\n\n`;

    // Создаем объект для хранения модулей
    finalContent += `    const modules = {};\n\n`;

    // Добавляем каждый модуль
    for (const [name, code] of Object.entries(modules)) {
        finalContent += `    // === Модуль: ${name} ===\n`;
        finalContent += `    (function() {\n`;
        finalContent += `        const module = {};\n`;
        finalContent += `        const exports = module.exports = {};\n\n`;
        finalContent += `        // Код модуля\n`;
        finalContent += code + '\n\n';
        finalContent += `        modules['${name}'] = module.exports;\n`;
        finalContent += `    })();\n\n`;
    }

    // Экспортируем модули в зависимости от типа
    if (isStartup) {
        // Для startup используем global (доступен только здесь)
        finalContent += `    // Экспорт для startup скриптов\n`;
        finalContent += `    if (typeof global !== 'undefined') {\n`;
        finalContent += `        global.${type}Modules = modules;\n`;
        finalContent += `    }\n`;
    } else if (isClient) {
        // Для client используем window
        finalContent += `    // Экспорт для client скриптов\n`;
        finalContent += `    if (typeof window !== 'undefined') {\n`;
        finalContent += `        window.${type}Modules = modules;\n`;
        finalContent += `    }\n`;
        // Сохраняем в глобальную переменную this (для случаев когда window недоступен)
        finalContent += `    if (typeof this !== 'undefined') {\n`;
        finalContent += `        this.${type}Modules = modules;\n`;
        finalContent += `    }\n`;
    } else if (isServer) {
        // Для server используем this (глобальный объект)
        finalContent += `    // Экспорт для server скриптов\n`;
        finalContent += `    if (typeof this !== 'undefined') {\n`;
        finalContent += `        this.${type}Modules = modules;\n`;
        finalContent += `    }\n`;
    }

    finalContent += `})();\n`;

    // Транспилируем через Babel
    try {
        const result = transform(finalContent, {
            ...BABEL_CONFIG,
            filename: `${type}.js`
        });

        fs.writeFileSync(outputFile, result.code, 'utf8');
        console.log(`   ✅ ${type}.js собран в ${outputFile}`);
        console.log(`   📁 Модулей: ${Object.keys(modules).length}`);
    } catch (error) {
        console.error(`   ❌ Ошибка при обработке ${type}.js:`, error.message);
    }
}

// Главная функция сборки
async function buildAll() {
    console.log('🚀 Начинаем сборку...');
    console.log('='.repeat(50));

    // Выбираем метод сборки
    const useVite = process.argv.includes('--vite');
    const useIIFE = process.argv.includes('--iife') || !useVite;

    if (useVite) {
        console.log('🔧 Используем Vite для сборки');
        for (const type of ['client', 'server', 'startup']) {
            await buildWithVite(type);
        }
    }

    if (useIIFE) {
        console.log('🔧 Используем IIFE для сборки');
        for (const type of ['client', 'server', 'startup']) {
            buildWithIIFE(type);
        }
    }

    console.log('='.repeat(50));
    console.log('✅ Сборка завершена!');
    console.log(`📁 Результаты сохранены в папках:`);
    Object.entries(OUTPUT_DIRS).forEach(([type, dir]) => {
        console.log(`   📂 ${type}: ${dir}`);
    });
}

// Запуск
buildAll();

// Для отслеживания изменений
if (process.argv.includes('--watch')) {
    console.log('👁️ Отслеживание изменений...');
    fs.watch(SRC_PATH, { recursive: true }, (event, filename) => {
        if (filename && filename.endsWith('.js')) {
            console.log(`📄 Изменен файл: ${filename}`);
            buildAll();
        }
    });
}
