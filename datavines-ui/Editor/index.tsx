// Editor component library entry
import React, { useState } from 'react';
import Editor from './components';
import './index.less';
import enUS from './locale/en_US';
import zhCN from './locale/zh_CN';
import { IDvEditorProps } from './type';
import { EditorProvider, useEditorActions, setEditorFn } from './store/editor';
import { useMount, useWatch } from './common';

const App = (props: IDvEditorProps) => {
    const {
        monacoConfig, baseURL, headers, id,
    } = props;
    const [loading, setLoading] = useState(true);
    const fns = useEditorActions({ setEditorFn });
    useMount(() => {
        fns.setEditorFn({
            monacoConfig,
            baseURL,
            headers: headers || {},
            id,
        });
        setLoading(false);
    });
    useWatch(id, () => {
        fns.setEditorFn({
            id,
        });
    });
    if (loading) {
        return null;
    }
    return <Editor {...props} />;
};

const DvEditor = (props: IDvEditorProps) => (
    <EditorProvider>
        <App {...props} />
    </EditorProvider>
);

export {
    DvEditor,
    enUS,
    zhCN,
};
