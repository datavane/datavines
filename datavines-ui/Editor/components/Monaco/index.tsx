import React, { useRef } from 'react';
import useEditor from '../../hooks/useEditor';
import { usePersistFn } from '../../common';

const Editor: React.FC = () => {
    const divEl = useRef<any>(null);
    const onChange = usePersistFn(() => {
        console.log('monacoEditor', monacoInstance);
        const $value = getValue();
        console.log('e', $value);
    });
    const { monacoInstance, getValue } = useEditor({
        elRef: divEl,
        value: 'select * from a.test',
        language: 'mysql',
        onChange,
        tableColumnHints: [['QRTZ_BLOB_TRIGGERS', ['SCHED_NAME', 'BLOB_DATA']]],
    });

    return <div id="container" className="Editor" style={{ height: 300 }} ref={divEl} />;
};

export default Editor;
