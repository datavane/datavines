import { useState, useRef } from 'react';
import type { languages, IRange } from 'monaco-editor';
import { useUnMount, useWatch, usePersistFn } from '../../common';
import { loadEditorMainModule } from '../../utils';
import { TUseEditor, TCodeEditor, THintsItem } from '../../type';
import { useEditorContextState } from '../../store/editor';
import {
    HINTS, SQL_STRING, getTableColumnHints, arrayRemoveRepeat,
} from './helper';

const useEditor = (props: TUseEditor) => {
    const {
        elRef, language, value, onChange, tableColumnHints,
    } = props;
    const [{ monacoConfig }] = useEditorContextState();
    const [monacoInstance, setMonacoInstance] = useState<TCodeEditor>();
    const providerRef = useRef<{ dispose:() => void }>();
    const [originHints] = useState(() => arrayRemoveRepeat([...HINTS, ...SQL_STRING]).sort());
    const getDefaultSuggestions = (positionBeforeText: string, range: IRange): languages.CompletionItem[] => {
        // eslint-disable-next-line no-useless-escape
        const $positionBeforeText = positionBeforeText.replace(/[\*\[\]@\$\(\)]/g, '').replace(/(\s+|\.)/g, ' ');
        const textArr = $positionBeforeText.split(' ');
        const activeVal = textArr[textArr.length - 1];
        console.log('textArr', textArr, activeVal);
        const rexp = new RegExp(`([^\\w]|^)${activeVal}\\w*`, 'gim');
        const match = value.match(rexp);
        const $hints = !match
            ? []
            : match.map((ele) => {
                const search = ele.search(new RegExp(activeVal, 'gim'));
                return ele.substr(search);
            });
        console.log('$hints', $hints);
        const hints = arrayRemoveRepeat([...originHints, ...$hints, ...getTableColumnHints(tableColumnHints || [])]).sort().filter((ele) => {
            const $rexp = new RegExp(ele.substr(0, activeVal.length), 'gim');
            return (match && match.length === 1 && ele === activeVal)
              || ele.length === 1
                ? false
                : activeVal.match($rexp);
        });
        console.log('hints111', hints);
        const $$hints = hints.map((ele) => ({
            label: ele,
            kind: window.monaco.languages.CompletionItemKind.Function,
            documentation: ele,
            insertText: ele,
            range: {
                ...range,
                startColumn: range.startColumn - activeVal.length,
            },
        }));
        console.log('$$hints222', $$hints);
        return $$hints;
    };
    const getTableSuggestions = (positionBeforeText: string, range: IRange): languages.CompletionItem[] => {
        const arr = positionBeforeText.split(' ');
        const currentVal = arr[arr.length - 1];
        const lastThree = arr[arr.length - 3] || '';
        const r = /select|SELECT/g;
        const s = /f|F|R|r|O|o|M|m/g;
        console.log('getTableSuggestions', lastThree, currentVal, currentVal.match(s), lastThree.match(r));
        if (
            lastThree
            && currentVal.match(s)
            && lastThree.match(r)
        ) {
            return getTableColumnHints(tableColumnHints || []).map((e) => ({
                label: e,
                kind: window.monaco.languages.CompletionItemKind.Folder,
                documentation: e,
                insertText: e,
                range: {
                    ...range,
                    startColumn: range.startColumn - currentVal.length,
                },
            }));
        }
        return [];
    };
    const monacoCreate = usePersistFn(() => {
        const $monacoInstance = window.monaco.editor.create(elRef.current, {
            value,
            language,
            theme: 'vs',
            cursorStyle: 'line',
            fontSize: 14,
            // glyphMargin: true,
            automaticLayout: true,
            overviewRulerBorder: false,
            foldingStrategy: 'indentation',
            suggestFontSize: 13,
            lineDecorationsWidth: 0,
            renderLineHighlight: 'none',
            fontFamily: 'PingFang SC, Microsoft YaHei',
            minimap: {
                enabled: false,
            },
        });
        setMonacoInstance($monacoInstance);
        providerRef.current = window.monaco.languages.registerCompletionItemProvider(language, {
            provideCompletionItems(model, position) {
                const lineNumber = {
                    startLineNumber: position.lineNumber,
                    endLineNumber: position.lineNumber,
                };
                const lineContent = model.getValueInRange({
                    ...lineNumber,
                    startColumn: 1,
                    endColumn: 1999,
                });
                const positionBeforeText = model.getValueInRange({
                    ...lineNumber,
                    startColumn: 1,
                    endColumn: position.column,
                });
                const positionAfterText = model.getValueInRange({
                    ...lineNumber,
                    startColumn: position.column,
                    endColumn: 1999,
                });
                console.log('内容', positionBeforeText, '-', positionAfterText);
                const range = {
                    ...lineNumber,
                    startColumn: position.column,
                    endColumn: 1999,
                };
                const defaultSuggestions = getDefaultSuggestions(positionBeforeText, range);
                const tableSuggestions = getTableSuggestions(positionBeforeText, range);
                console.log('lineContent', tableSuggestions, defaultSuggestions);
                return {
                    // suggestions: defaultSuggestions || [],
                    suggestions: [...tableSuggestions, ...defaultSuggestions] || [],
                };
            },
        });
        $monacoInstance?.onDidChangeModelContent((e: any) => {
            if (onChange) {
                onChange(e);
            }
        });
    });

    useWatch([language], async () => {
        if (monacoInstance) {
            dispose();
            monacoCreate();
            return;
        }
        try {
            await loadEditorMainModule(monacoConfig);
            monacoCreate();
        } catch (error) {
        }
    }, { immediate: true });
    useWatch(value, () => {
        monacoInstance?.setValue(value);
    });
    const dispose = usePersistFn(() => {
        if (monacoInstance?.getModel()) {
            monacoInstance?.getModel()?.dispose();
        }
        monacoInstance?.dispose();
        if (providerRef.current) {
            providerRef.current.dispose();
        }
    });
    useUnMount(() => {
        dispose();
    });
    const getValue = usePersistFn(() => monacoInstance?.getValue());
    return {
        monacoInstance,
        getValue,
    };
};

export default useEditor;
