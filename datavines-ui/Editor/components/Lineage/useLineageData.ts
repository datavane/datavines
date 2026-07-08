import { useState, useEffect, useCallback } from 'react';
import useRequest from '../../hooks/useRequest';

export interface CatalogEntityInstanceInfo {
    id?: number;
    uuid: string;
    datasourceId?: number;
    type?: string;
    fullyQualifiedName?: string;
    displayName?: string;
    description?: string;
}

export interface LineageEntityNodeInfo {
    id?: number;
    uuid: string;
    datasourceId?: number;
    type?: string;
    fullyQualifiedName?: string;
    displayName?: string;
    description?: string;
    datasource?: CatalogEntityInstanceInfo;
    database?: CatalogEntityInstanceInfo;
    schema?: CatalogEntityInstanceInfo;
    catalog?: CatalogEntityInstanceInfo;
    columns?: CatalogEntityInstanceInfo[];
    hasNextNode?: boolean;
}

export interface CatalogEntityColumnLineageDetail {
    fromChildren?: CatalogEntityInstanceInfo[];
    function?: string;
    toChild?: CatalogEntityInstanceInfo;
}

export interface CatalogEntityLineageDetail {
    childRelDetailList?: CatalogEntityColumnLineageDetail[];
    description?: string;
    sourceType?: string;
    sqlQuery?: string;
}

export interface LineageEntityEdgeInfo {
    uuid?: string;
    fromEntity?: CatalogEntityInstanceInfo;
    toEntity?: CatalogEntityInstanceInfo;
    description?: string;
    lineageDetail?: CatalogEntityLineageDetail;
}

export interface CatalogEntityLineageVO {
    currentNode?: LineageEntityNodeInfo;
    nodes: LineageEntityNodeInfo[];
    edges: LineageEntityEdgeInfo[];
}

export interface UseLineageDataReturn {
    data: CatalogEntityLineageVO | null;
    loading: boolean;
    error: string | null;
    reload: () => void;
    loadByUUID: (uuid: string) => void;
}

export default function useLineageData(tableUuid: string): UseLineageDataReturn {
    const { $http } = useRequest();
    const [data, setData] = useState<CatalogEntityLineageVO | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [currentUuid, setCurrentUuid] = useState(tableUuid);

    const fetchLineage = useCallback(async (uuid: string) => {
        if (!uuid) return;
        setLoading(true);
        setError(null);
        try {
            const res = await $http.get(`/catalog/lineage/getByUUID/${uuid}`);
            setData(res || null);
        } catch (e: any) {
            setError(e?.msg || 'Failed to load lineage');
            setData(null);
        } finally {
            setLoading(false);
        }
    }, [$http]);

    useEffect(() => {
        if (tableUuid) {
            setCurrentUuid(tableUuid);
            fetchLineage(tableUuid);
        }
    }, [tableUuid]);

    const reload = useCallback(() => {
        fetchLineage(currentUuid);
    }, [currentUuid, fetchLineage]);

    const loadByUUID = useCallback((uuid: string) => {
        setCurrentUuid(uuid);
        fetchLineage(uuid);
    }, [fetchLineage]);

    return { data, loading, error, reload, loadByUUID };
}
