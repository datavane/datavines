import React from 'react';
import useRequest from '../../../hooks/useRequest';
import { useEditorActions, setEditorFn, useEditorContextState } from '../../../store/editor';
import { usePersistFn } from '../../../common';
import { IDvDataBaseItem } from '../../../type';

type TuseTableCloumn = {
    $setExpandedKeys: (key: React.Key, isCancel?: boolean) => void;
}

const useTableCloumn = ({ $setExpandedKeys }: TuseTableCloumn) => {
    const [{ databases, id }] = useEditorContextState();
    const fns = useEditorActions({ setEditorFn });
    const { $http } = useRequest();
    const onRequestTable = usePersistFn(async (databaseName, key: React.Key) => {
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
            $setExpandedKeys(key);
        } catch (error) {
        }
    });
    const onRequestCloumn = usePersistFn(async (databaseName, tableName, key: React.Key) => {
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
            $setExpandedKeys(key);
        } catch (error) {
        }
    });

    return {
        onRequestTable,
        onRequestCloumn,
    };
};

export default useTableCloumn;
