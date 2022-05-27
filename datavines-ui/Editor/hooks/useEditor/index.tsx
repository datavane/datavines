/* eslint-disable no-plusplus */
import { useEffect, useMemo } from 'react';
import useUnMount from '../../common/useUnMount';
import { TUseEditor } from '../../type';

const useEditor = (props: TUseEditor) => {
    const {
        elRef, language, value, onChange,
    } = props;
    let monacoEditor: any;
    const hints = useMemo(() => [
        'a',
    ], []);

    const triggerSuggest = (newVal: string) => {
        if (newVal.length > 0) {
            monacoEditor.trigger('Tips:', 'editor.action.triggerSuggest', {});
        }
    };

    useEffect(() => {
        (window as any).require(['vs/editor/editor.main'], () => {
            // @ts-ignore
            monacoEditor = window.monaco.editor.create(elRef.current, {
                value,
                language,
            });
            (window as any).monacoEditor = monacoEditor;
            // @ts-ignore
            window.monaco.languages.registerCompletionItemProvider(language, {
                // provideCompletionItems: (model, positionn) => {
                //     const suggestions: any = [];
                //     // @ts-ignore
                //     for (let i = 0; i < suggestions.length; i++) {
                //         const v = suggestions[i];
                //         delete v.range;
                //     }
                //     return {
                //         suggestions,
                //     };
                // },
                // resolveCompletionItem() {
                //     return null;
                // },
                provideCompletionItems(model: any, position: any) {
                    console.log(model, position);
                    const line = position.lineNumber;
                    const { column } = position;
                    const content = model.getLineContent(line);
                    const sym = content[column - 2];
                    if (sym === '$') {
                        return {
                            suggestions: [{
                                label: '${_DB',
                                kind: (window as any).monaco.languages.CompletionItemKind.Function,
                                insertText: '{_DB',
                                detail: '',
                            }],
                        };
                    }
                    if (sym === ':') {
                        return {
                            suggestions: [{
                                label: ':abb',
                                kind: (window as any).monaco.languages.CompletionItemKind.Function,
                                insertText: 'abb',
                                detail: '',
                            },
                            {
                                label: ':bc',
                                kind: (window as any).monaco.languages.CompletionItemKind.Function,
                                insertText: 'bc',
                                detail: '',
                            }],
                        };
                    }
                    return {
                        suggestions: [],
                    };
                },
                triggerCharacters: ['$', ':', '.'],
            });
            monacoEditor.onDidChangeModelContent((e: any) => {
                console.log(e);
                // const $value = monacoEditor.getValue();
                // triggerSuggest($value);
                // const caretOffset = e.changes[0].rangeOffset; // 获取光标位置
                // if (onChange) {
                //     onChange({
                //         value: monacoEditor.getValue(),
                //         caretOffset,
                //         e,
                //     });
                // }
            });
        });
    }, [language]);
    useUnMount(() => {
        monacoEditor?.dispose();
    });
    return {
        monacoEditor,
    };
};

export default useEditor;
