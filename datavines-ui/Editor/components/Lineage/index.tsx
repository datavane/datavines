import React, { useState, useCallback, useEffect } from 'react';
import { Spin, Empty, Alert, Button, message } from 'antd';
import { PlusOutlined, NodeIndexOutlined } from '@ant-design/icons';
import { useIntl } from 'react-intl';
import useLineageData, { LineageEntityNodeInfo, LineageEntityEdgeInfo, CatalogEntityLineageVO } from './useLineageData';
import LineageGraph from './LineageGraph';
import LineageToolbar from './LineageToolbar';
import LineageDetailDrawer from './LineageDetailDrawer';
import AddLineageModal from './AddLineageModal';
import SqlParseModal from './SqlParseModal';
import useRequest from '../../hooks/useRequest';
import { useEditorContextState } from '../../store/editor';
import './LineageGraph.less';

interface LineageProps {
    tableUuid: string;
}

const Lineage: React.FC<LineageProps> = ({ tableUuid }) => {
    const intl = useIntl();
    const { $http } = useRequest();
    const [{ workspaceId }] = useEditorContextState();
    const { data, loading, error, reload, loadByUUID } = useLineageData(tableUuid);

    const [direction, setDirection] = useState('all');
    const [drawerVisible, setDrawerVisible] = useState(false);
    const [selectedNode, setSelectedNode] = useState<LineageEntityNodeInfo | null>(null);
    const [selectedEdge, setSelectedEdge] = useState<LineageEntityEdgeInfo | null>(null);
    const [addModalVisible, setAddModalVisible] = useState(false);
    const [sqlModalVisible, setSqlModalVisible] = useState(false);
    const [datasourceList, setDatasourceList] = useState<any[]>([]);

    useEffect(() => {
        $http.get('/datasource/page', { workSpaceId: workspaceId, pageNumber: 1, pageSize: 9999 })
            .then((res: any) => {
                setDatasourceList(res?.records || []);
            })
            .catch(() => {});
    }, [workspaceId]);

    const getFilteredData = useCallback((): CatalogEntityLineageVO | null => {
        if (!data) return null;
        if (direction === 'all') return data;

        const currentUuid = data.currentNode?.uuid;
        const filteredEdges = (data.edges || []).filter((edge) => {
            if (direction === 'upstream') return edge.toEntity?.uuid === currentUuid;
            return edge.fromEntity?.uuid === currentUuid;
        });

        const relatedUuids = new Set<string>();
        relatedUuids.add(currentUuid || '');
        filteredEdges.forEach((edge) => {
            if (edge.fromEntity?.uuid) relatedUuids.add(edge.fromEntity.uuid);
            if (edge.toEntity?.uuid) relatedUuids.add(edge.toEntity.uuid);
        });

        return {
            currentNode: data.currentNode,
            nodes: (data.nodes || []).filter((n) => relatedUuids.has(n.uuid)),
            edges: filteredEdges,
        };
    }, [data, direction]);

    const handleNodeClick = useCallback((node: LineageEntityNodeInfo) => {
        setSelectedNode(node);
        setSelectedEdge(null);
        setDrawerVisible(true);
    }, []);

    const handleEdgeClick = useCallback((edge: LineageEntityEdgeInfo) => {
        setSelectedEdge(edge);
        setSelectedNode(null);
        setDrawerVisible(true);
    }, []);

    const handleExpandNode = useCallback((uuid: string) => {
        loadByUUID(uuid);
    }, [loadByUUID]);

    const handleViewAsCenter = useCallback((uuid: string) => {
        setDrawerVisible(false);
        loadByUUID(uuid);
    }, [loadByUUID]);

    const handleDeleteEdge = useCallback(async (fromUuid: string, toUuid: string) => {
        try {
            await $http.delete(`/catalog/lineage/${fromUuid}/${toUuid}`);
            message.success(intl.formatMessage({ id: 'lineage_delete' }) + ' ✓');
            setDrawerVisible(false);
            reload();
        } catch (e: any) {
            if (e?.msg) message.error(e.msg);
        }
    }, [$http, reload, intl]);

    const handleSearch = useCallback((value: string) => {
        if (!data || !value) return;
        const found = data.nodes.find(
            (n) => n.displayName?.toLowerCase().includes(value.toLowerCase()),
        );
        if (found) {
            setSelectedNode(found);
            setSelectedEdge(null);
            setDrawerVisible(true);
        }
    }, [data]);

    const handleFitView = useCallback(() => {}, []);

    const handleAddSuccess = useCallback(() => {
        setAddModalVisible(false);
        reload();
    }, [reload]);

    const handleSqlSuccess = useCallback(() => {
        setSqlModalVisible(false);
        reload();
    }, [reload]);

    if (loading && !data) {
        return (
            <div className="lineage-loading-container">
                <Spin size="large" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="lineage-error-container">
                <Alert
                    type="error"
                    showIcon
                    message={error}
                    action={<Button size="small" onClick={reload}>{intl.formatMessage({ id: 'lineage_refresh' })}</Button>}
                />
            </div>
        );
    }

    const filteredData = getFilteredData();
    const isEmpty = !filteredData
        || ((filteredData.nodes?.length || 0) === 0);

    return (
        <div className="lineage-wrapper">
            <LineageToolbar
                direction={direction}
                onDirectionChange={setDirection}
                onRefresh={reload}
                onFitView={handleFitView}
                onAddLineage={() => setAddModalVisible(true)}
                onParseSql={() => setSqlModalVisible(true)}
                onSearch={handleSearch}
            />

            {isEmpty ? (
                <div className="lineage-empty-container">
                    <Empty
                        image={<NodeIndexOutlined style={{ fontSize: 56, color: '#c7d2e0' }} />}
                        description={
                            <span style={{ color: '#6e7191' }}>
                                {intl.formatMessage({ id: 'lineage_empty_desc' })}
                            </span>
                        }
                    >
                        <Button
                            type="primary"
                            icon={<PlusOutlined />}
                            onClick={() => setAddModalVisible(true)}
                        >
                            {intl.formatMessage({ id: 'lineage_add' })}
                        </Button>
                    </Empty>
                </div>
            ) : (
                <Spin spinning={loading}>
                    <LineageGraph
                        data={filteredData}
                        onNodeClick={handleNodeClick}
                        onEdgeClick={handleEdgeClick}
                        onExpandNode={handleExpandNode}
                    />
                </Spin>
            )}

            <LineageDetailDrawer
                visible={drawerVisible}
                onClose={() => setDrawerVisible(false)}
                selectedNode={selectedNode}
                selectedEdge={selectedEdge}
                onViewAsCenter={handleViewAsCenter}
                onDeleteEdge={handleDeleteEdge}
            />

            <AddLineageModal
                visible={addModalVisible}
                onClose={() => setAddModalVisible(false)}
                onSuccess={handleAddSuccess}
                datasourceList={datasourceList}
            />

            <SqlParseModal
                visible={sqlModalVisible}
                onClose={() => setSqlModalVisible(false)}
                onSuccess={handleSqlSuccess}
                datasourceList={datasourceList}
            />
        </div>
    );
};

export default Lineage;
