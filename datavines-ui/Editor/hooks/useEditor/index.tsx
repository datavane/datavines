import { useState } from 'react';
import { useUnMount, useWatch, usePersistFn } from '../../common';
import { loadEditorMainModule } from '../../utils';
import { TUseEditor, TCodeEditor } from '../../type';
import { useEditorContextState } from '../../store/editor';

const useEditor = (props: TUseEditor) => {
    const {
        elRef, language, value, onChange,
    } = props;
    const [{ monacoConfig }] = useEditorContextState();
    const [monacoEditor, setMonacoEditor] = useState<TCodeEditor>();
    const monacoCreate = usePersistFn(() => {
        const $monacoEditor = window.monaco.editor.create(elRef.current, {
            value,
            language,
        });
        setMonacoEditor($monacoEditor);
        window.monaco.languages.registerCompletionItemProvider(language, {
            provideCompletionItems() {
                return {
                    suggestions: [],
                };
            },
        });
        $monacoEditor?.onDidChangeModelContent((e: any) => {
            if (onChange) {
                onChange(e);
            }
        });
    });

    useWatch([language], async () => {
        if (monacoEditor) {
            monacoCreate();
            return;
        }
        try {
            await loadEditorMainModule(monacoConfig);
            monacoCreate();
        } catch (error) {
        }
    }, { immediate: true });
    useUnMount(() => {
        monacoEditor?.dispose();
    });
    return {
        monacoEditor,
    };
};

export default useEditor;
