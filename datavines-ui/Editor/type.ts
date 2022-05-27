export type TUseEditor = {
    elRef: any,
    value: string,
    language: 'sql',
    onChange?: (...args: any[]) => any
};

export interface IMonacoConfig{
    paths: {
        vs: string,
        [key: string]: any;
    }
    [key: string]: any;
}

export interface IDvEditorProps {
    monacoConfig: IMonacoConfig,
    baseURL: string,
    headers?: Record<string, any>,
    id: number | string | null,
}

export interface IDvDataBaseItem{
    comment: string,
    name: string,
    type: string,
    children?: IDvDataBaseItem[]
}
