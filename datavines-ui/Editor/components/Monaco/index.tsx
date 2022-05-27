/* eslint-disable prefer-arrow-callback */
/* eslint-disable global-require */
/* eslint-disable no-restricted-globals */
import React, { useRef, useEffect } from 'react';
// import * as monaco from 'monaco-editor';
import useEditor from '../../hooks/useEditor';

const Editor: React.FC = () => {
    const divEl = useRef<any>(null);
    const { monacoEditor } = useEditor({ elRef: divEl, value: 'select * from a.test', language: 'sql' });
    // let editor: any;
    // useEffect(() => {
    //     if (divEl.current) {
    //         // editor = monaco.editor.create(divEl.current, {
    //         //     value: ['function x() {', '\tconsole.log("Hello world!");', '}'].join('\n'),
    //         //     language: 'typescript',
    //         // });

    //     }
    //     (window as any).require(['vs/editor/editor.main'], function () {
    //         console.log('lai l');
    //         // @ts-ignore
    //         editor = window.monaco.editor.create(document.getElementById('container'), {
    //             value: ['function x() {', '\tconsole.log("Hello world!");', '}'].join('\n'),
    //             language: 'javascript',
    //         });
    //     });
    //     return () => {
    //         editor.dispose();
    //     };
    // }, []);
    return <div id="container" className="Editor" style={{ height: 300 }} ref={divEl} />;
};

export default Editor;
