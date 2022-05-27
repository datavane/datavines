import React from 'react';
import { Tree } from 'antd';
import { DatabaseOutlined, TableOutlined } from '@ant-design/icons';
import { useMetricModal } from '../MetricModal';
import useRequest from '../../hooks/useRequest';
import { useEditorActions, setEditorFn, useEditorContextState } from '../../store/editor';
import { usePersistFn } from '../../common';
import { IDvDataBaseItem } from '../../type';

const Index = () => {
    const { Render: RenderModal, show } = useMetricModal();
    const [{ databases, id }] = useEditorContextState();
    const { $http } = useRequest();
    const fns = useEditorActions({ setEditorFn });
    const onRequestTable = usePersistFn(async (databaseName) => {
        try {
            const fileDatabaseName = databases.find((item) => ((item.name === databaseName) && (item.children || []).length > 0));
            if (fileDatabaseName) {
                return;
            }
            const res = await $http.get(`/datasource/${id}/${databaseName}/tables`);
            const data = databases.reduce<IDvDataBaseItem[]>((prev, cur) => {
                if (cur.name === databaseName) {
                    cur.children = res;
                }
                prev.push({ ...cur });
                return prev;
            }, []);
            fns.setEditorFn({ databases: data });
        } catch (error) {
        }
    });
    const onRequestCloumn = usePersistFn(async (databaseName, tableName) => {
        try {
            const fileDatabase = databases.find((item) => (item.name === databaseName));
            if (fileDatabase) {
                const findTable = (fileDatabase.children || []).find((item) => (item.name === tableName) && (item.children || []).length > 0);
                if (findTable) {
                    return;
                }
            }
            const res = await $http.get(`/datasource/${id}/${databaseName}/${tableName}/columns`);
            const data = databases.reduce<IDvDataBaseItem[]>((prev, cur) => {
                if (cur.name === databaseName) {
                    const children = (cur.children || []).map((item) => {
                        if (item.name === tableName) {
                            return {
                                ...item,
                                children: res?.columns || [],
                            };
                        }
                        return { ...item };
                    });
                    cur.children = children;
                }
                prev.push({ ...cur });
                return prev;
            }, []);
            fns.setEditorFn({ databases: data });
        } catch (error) {
        }
    });

    const renderSingle = (item: IDvDataBaseItem) => ({
        title: <span>{item.name}</span>,
        key: item.name,
        dataName: item.name,
        type: item.type,
        icon: <DatabaseOutlined />,
        children: (item.children || []).map((tableItem) => ({
            title: <span>{tableItem.name}</span>,
            key: `${item.name}_${tableItem.name}`,
            dataName: tableItem.name,
            parentName: item.name,
            type: tableItem.type,
            icon: <TableOutlined />,
            children: (tableItem.children || []).map((fieldItem) => ({
                title: <span>{fieldItem.name}</span>,
                key: `${item.name}_${tableItem.name}_${fieldItem.name}`,
                dataName: fieldItem.name,
                type: fieldItem.type,
            })),
        })),
    });
    const onSelect = (data: any, e: any) => {
        // console.log('data', data, e);
        if (e.node.type === 'DATABASE') {
            onRequestTable(e.node.dataName);
        } else if (e.node.type === 'TABLE') {
            onRequestCloumn(e.node.parentName, e.node.dataName);
        }
    };
    return (
        <div>
            <Tree
                showIcon
                onSelect={onSelect}
                treeData={databases.map((item) => renderSingle(item))}
            />
            <RenderModal />
        </div>
    );
};

export default Index;
