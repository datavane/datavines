import type {
    editor, languages, Uri, CancellationTokenSource, Emitter, KeyCode, KeyMod, MarkerSeverity, MarkerTag, Position, Range, Selection, SelectionDirection,
} from 'monaco-editor';

export type TCodeEditor = editor.ICodeEditor;
declare global {
    interface Window {
        monaco: {
            editor: typeof editor,
            languages: typeof languages,
            Uri: typeof Uri,
            CancellationTokenSource: typeof CancellationTokenSource,
            Emitter: typeof Emitter,
            KeyCode: typeof KeyCode,
            KeyMod: typeof KeyMod,
            MarkerSeverity: typeof MarkerSeverity,
            MarkerTag: typeof MarkerTag,
            Position: typeof Position,
            Range: typeof Range,
            Selection: typeof Selection,
            SelectionDirection: typeof SelectionDirection,
        }
    }
}
export type TSqlType = 'mysql' | 'clickhouse' | 'hive' | 'impala' | 'postgresql';

export type THintsItem = [str:string, arg:string[]]
export type TUseEditor = {
    elRef: any,
    value: string,
    language: TSqlType,
    tableColumnHints: THintsItem[],
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
