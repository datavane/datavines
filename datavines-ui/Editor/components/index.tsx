import React, { useRef, useState, useMemo } from 'react';
import { Divider } from 'antd';
import { useIntl } from 'react-intl';
import Tree from './Tree';
import Monaco from './Monaco';
import { IDvEditorProps, IDvSqlTable } from '../type';
import useRequest from '../hooks/useRequest';
import { useWatch, usePersistFn, useMount } from '../common';
import { useEditorActions, setEditorFn, useEditorContextState } from '../store/editor';
import SqlTable from './SqlTable';
import './index.less';

const Index = (props: IDvEditorProps) => {
    const { id } = props;
    const [context] = useEditorContextState();
    const [tableData, setTableData] = useState<IDvSqlTable>({ resultList: [], columns: [] });
    const { $http } = useRequest();
    const monacoRef = useRef<any>();
    const containerRef = useRef<any>();
    const intl = useIntl();
    const fns = useEditorActions({ setEditorFn });
    const [height, setHeight] = useState(null);
    const monacoStyle = useMemo(() => {
        if (!height) {
            return {};
        }
        const $height = `calc(100vh - 110px - ${(height * 2) / 5}px)`;
        return {
            height: 260,
        };
    }, [height]);
    const sqlStyle = useMemo(() => {
        if (!height) {
            return {};
        }
        const $height = `calc(100vh - 110px - ${(height * 3) / 5}px)`;
        return {
            // height: $height,
        };
    }, [height]);
    const getDatabases = usePersistFn(async () => {
        try {
            const res = await $http.get(`/datasource/${id}/databases`);
            fns.setEditorFn({ databases: res || [] });
        } catch (error) {
        }
    });
    useMount(() => {
        setHeight(containerRef.current.clientHeight);
    });
    useWatch(id, async () => {
        getDatabases();
    }, { immediate: true });

    const onRun = async () => {
        const val = monacoRef.current.getValue();
        if (!val) {
            return;
        }
        try {
            const params = {
                datasourceId: context.id,
                script: val,
                variables: '',
            };
            const res = await $http.post('/datasource/execute', params);
            console.log('res', res);
            setTableData(res || {});
        } catch (error) {
        }
    };

    return (
        <div className="dv-editor" ref={containerRef}>
            <div className="dv-editor-left">
                <Tree getDatabases={getDatabases} />
            </div>
            <div className="dv-editor-right">
                <div className="dv-editor__header">
                    <a onClick={onRun}>{intl.formatMessage({ id: 'dv_metric_run' })}</a>
                </div>
                <Monaco monacoRef={monacoRef} style={monacoStyle} />
                <Divider />
                <SqlTable style={sqlStyle} tableData={tableData} />
            </div>
        </div>
    );
};

export default Index;
