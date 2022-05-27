import React, { useRef } from 'react';
import useEditor from '../../hooks/useEditor';
import { usePersistFn } from '../../common';

const Editor: React.FC = () => {
    const divEl = useRef<any>(null);
    const onChange = usePersistFn((e) => {
        console.log('monacoEditor', monacoEditor);
        const $value = monacoEditor?.getValue();
        console.log('e', $value);
    });
    const { monacoEditor } = useEditor({
        elRef: divEl, value: 'select * from a.test', language: 'mysql', onChange,
    });

    return <div id="container" className="Editor" style={{ height: 300 }} ref={divEl} />;
};

export default Editor;
