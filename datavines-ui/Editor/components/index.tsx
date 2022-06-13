import React from 'react';
import Tree from './Tree';
import Monaco from './Monaco';
import { IDvEditorProps } from '../type';
import useRequest from '../hooks/useRequest';
import { useWatch, usePersistFn } from '../common';
import { useEditorActions, setEditorFn } from '../store/editor';
import './index.less';

const Index = (props: IDvEditorProps) => {
    const { id } = props;
    const { $http } = useRequest();
    const fns = useEditorActions({ setEditorFn });
    const getDatabases = usePersistFn(async () => {
        try {
            const res = await $http.get(`/datasource/${id}/databases`);
            fns.setEditorFn({ databases: res || [] });
        } catch (error) {
        }
    });
    useWatch(id, async () => {
        getDatabases();
    }, { immediate: true });

    return (
        <div className="dv-editor">
            <div className="dv-editor-left">
                <Tree getDatabases={getDatabases} />
            </div>
            <div className="dv-editor-right">
                <div className="dv-editor__header">
                    <a>Run</a>
                </div>
                <div className="dv-editor__monaco">
                    <Monaco />
                </div>
                <div className="dv-editor__table">
                    border
                </div>
            </div>
        </div>
    );
};

export default Index;
