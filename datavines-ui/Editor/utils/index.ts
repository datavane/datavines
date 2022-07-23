import { IMonacoConfig } from '../type';

const createScript = (src: string) => {
    const script = document.createElement('script');
    script.src = src;
    return script;
};

export const editorLoaderScript = (config: IMonacoConfig): Promise<boolean> => new Promise((resolve, reject) => {
    const script = createScript(`${config.paths.vs}/loader.js`);
    script.onload = () => {
        resolve(true);
    };
    script.onerror = (error) => {
        reject(error);
    };
    document.body.appendChild(script);
});

// eslint-disable-next-line no-async-promise-executor
export const loadEditorMainModule = async (config: IMonacoConfig) => new Promise(async (resolve, reject) => {
    try {
        await editorLoaderScript(config);
        console.log('(window as any).require', (window as any).require);
        (window as any).require.config(config);
        (window as any).require(
            ['vs/editor/editor.main'],
            (monaco: any) => {
                resolve(monaco);
            },
            (error: Error) => {
                console.log('33333', error);
                reject(error);
            },
            () => {
                console.log('123');
            },
        );
    } catch (error) {
        console.log('3333333333', error);
        reject(error);
    }
});
