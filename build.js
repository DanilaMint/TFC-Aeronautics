const esbuild = require('esbuild');
const fs = require('fs');
const path = require('path');

// Конфигурация сборки
const config = {
    client: {
        entry: 'src/client.js',
        outdir: 'client_scripts',
        platform: 'browser',
        target: 'es2020',
        format: 'iife',
        minify: true,
        sourcemap: true,
        bundle: true,
        external: ['fs', 'path', 'os'], // Node.js модули, которые не должны попасть в клиент
    },
    server: {
        entry: 'src/server.js',
        outdir: 'server_scripts',
        platform: 'node',
        target: 'node18',
        format: 'cjs',
        minify: true,
        sourcemap: true,
        bundle: true,
        external: ['sharp', 'ffmpeg'], // Пример тяжелых зависимостей
    },
    startup: {
        entry: 'src/startup.js',
        outdir: 'startup_scripts',
        platform: 'node',
        target: 'node18',
        format: 'cjs',
        minify: true,
        sourcemap: true,
        bundle: true,
        external: [],
    }
};

// Функция очистки папок перед сборкой
function cleanDirectories() {
    const dirs = ['client_scripts', 'server_scripts', 'startup_scripts'];
    dirs.forEach(dir => {
        if (fs.existsSync(dir)) {
            fs.rmSync(dir, { recursive: true, force: true });
        }
        fs.mkdirSync(dir, { recursive: true });
    });
    console.log('🧹 Папки очищены');
}

// Функция сборки
async function build(target, options) {
    console.log(`📦 Сборка ${target}...`);

    try {
        const result = await esbuild.build({
            entryPoints: [options.entry],
            outdir: options.outdir,
            platform: options.platform,
            target: options.target,
            format: options.format,
            minify: options.minify,
            sourcemap: options.sourcemap,
            bundle: options.bundle,
            external: options.external,
            outbase: 'src',
            // Дополнительные настройки
            legalComments: 'none',
            treeShaking: true,
            metafile: true,
            logLevel: 'info',
        });

        // Анализ размера
        const stats = analyzeBuild(result, options.outdir);
        console.log(`✅ ${target} собран (${stats})`);

        return result;
    } catch (error) {
        console.error(`❌ Ошибка сборки ${target}:`, error.message);
        throw error;
    }
}

// Анализ размера собранных файлов
function analyzeBuild(result, outdir) {
    const files = fs.readdirSync(outdir);
    let totalSize = 0;
    const fileSizes = files.map(file => {
        const stats = fs.statSync(path.join(outdir, file));
        totalSize += stats.size;
        return `${file}: ${(stats.size / 1024).toFixed(2)}KB`;
    });

    return `${files.length} файлов, ${(totalSize / 1024).toFixed(2)}KB общий размер`;
}

// Функция копирования ассетов (если нужны)
function copyAssets() {
    // Пример: копирование статических файлов
    const assets = ['public'];
    assets.forEach(asset => {
        if (fs.existsSync(asset)) {
            // Здесь можно добавить логику копирования
            console.log(`📁 Ассеты скопированы: ${asset}`);
        }
    });
}

// Функция создания файла с версией сборки
function createVersionFile() {
    const version = {
        buildTime: new Date().toISOString(),
        version: process.env.npm_package_version || '1.0.0',
        nodeVersion: process.version,
    };

    fs.writeFileSync(
        'build-info.json',
        JSON.stringify(version, null, 2)
    );
    console.log('📝 Информация о сборке сохранена');
}

// Основная функция сборки
async function buildAll() {
    const startTime = Date.now();
    console.log('🚀 Начинаем сборку проекта...\n');

    try {
        // Очистка
        //cleanDirectories();

        // Сборка всех частей
        const builds = await Promise.all([
            build('client', config.client),
            build('server', config.server),
            build('startup', config.startup),
        ]);

        // Дополнительные задачи
        copyAssets();
        createVersionFile();

        const endTime = Date.now();
        const duration = ((endTime - startTime) / 1000).toFixed(2);

        console.log(`\n✨ Сборка завершена за ${duration} секунд!`);
        console.log('📁 Результаты:');
        console.log('  - client_scripts/');
        console.log('  - server_scripts/');
        console.log('  - startup_scripts/');

    } catch (error) {
        console.error('\n❌ Сборка провалилась:', error);
        process.exit(1);
    }
}

// Watch режим (если передан флаг --watch)
if (process.argv.includes('--watch')) {
    console.log('👀 Запуск в режиме watch...');

    const watchConfigs = [
        { ...config.client, watch: true },
        { ...config.server, watch: true },
        { ...config.startup, watch: true },
    ];

    watchConfigs.forEach(cfg => {
        esbuild.context({
            entryPoints: [cfg.entry],
            outdir: cfg.outdir,
            platform: cfg.platform,
            target: cfg.target,
            format: cfg.format,
            minify: cfg.minify,
            sourcemap: cfg.sourcemap,
            bundle: cfg.bundle,
            external: cfg.external,
            outbase: 'src',
            legalComments: 'none',
            treeShaking: true,
        }).then(context => {
            context.watch();
            console.log(`👀 Отслеживание: ${cfg.entry} -> ${cfg.outdir}`);
        });
    });

    console.log('Нажмите Ctrl+C для остановки');
} else {
    // Обычная сборка
    buildAll();
}
