import React from 'react';
import {
    Drawer, Tag, Button, Popconfirm, Typography, Divider,
} from 'antd';
import {
    ArrowRightOutlined, DeleteOutlined, AimOutlined, DatabaseOutlined,
} from '@ant-design/icons';
import { useIntl } from 'react-intl';
import { LineageEntityNodeInfo, LineageEntityEdgeInfo } from './useLineageData';

const { Text, Title } = Typography;

interface LineageDetailDrawerProps {
    visible: boolean;
    onClose: () => void;
    selectedNode?: LineageEntityNodeInfo | null;
    selectedEdge?: LineageEntityEdgeInfo | null;
    onViewAsCenter?: (uuid: string) => void;
    onDeleteEdge?: (fromUuid: string, toUuid: string) => void;
}

const SOURCE_TYPE_COLORS: Record<string, string> = {
    MANUAL: 'blue',
    SQL_PARSER: 'green',
    SPARK_LISTENER: 'orange',
    FLINK_SQL_LINEAGE: 'purple',
    manual: 'blue',
    sql_parser: 'green',
};

const LineageDetailDrawer: React.FC<LineageDetailDrawerProps> = ({
    visible, onClose, selectedNode, selectedEdge, onViewAsCenter, onDeleteEdge,
}) => {
    const intl = useIntl();

    const renderNodeDetail = () => {
        if (!selectedNode) return null;
        const columns = selectedNode.columns || [];

        return (
            <div className="lineage-drawer">
                {/* Header */}
                <div style={{ marginBottom: 20 }}>
                    <Title level={5} style={{ margin: 0 }}>
                        <DatabaseOutlined style={{ marginRight: 8, color: '#4169E1' }} />
                        {selectedNode.displayName}
                    </Title>
                    {selectedNode.datasource?.type && (
                        <Tag color="blue" style={{ marginTop: 8 }}>
                            {selectedNode.datasource.type.toUpperCase()}
                        </Tag>
                    )}
                </div>

                {/* FQN */}
                <div className="drawer-section">
                    <div className="drawer-section-title">Fully Qualified Name</div>
                    <div className="drawer-fqn">
                        {selectedNode.fullyQualifiedName || '-'}
                    </div>
                </div>

                {/* Metadata */}
                <div className="drawer-section">
                    <div className="drawer-section-title">
                        {intl.formatMessage({ id: 'lineage_select_datasource' })}
                    </div>
                    <Text>{selectedNode.datasource?.displayName || '-'}</Text>
                </div>
                <div className="drawer-section">
                    <div className="drawer-section-title">
                        {intl.formatMessage({ id: 'lineage_select_database' })}
                    </div>
                    <Text>{selectedNode.database?.displayName || '-'}</Text>
                </div>

                {/* Columns */}
                {columns.length > 0 && (
                    <div className="drawer-section">
                        <div className="drawer-section-title">
                            {intl.formatMessage({ id: 'dv_metric_column' })} ({columns.length})
                        </div>
                        <div style={{
                            display: 'flex', flexWrap: 'wrap', gap: 6, maxHeight: 200, overflow: 'auto',
                        }}
                        >
                            {columns.map((col) => (
                                <Tag key={col.uuid} style={{
                                    margin: 0, borderRadius: 4, background: '#f5f6fa', border: '1px solid #e8ecf1',
                                }}
                                >
                                    {col.displayName}
                                </Tag>
                            ))}
                        </div>
                    </div>
                )}

                <Divider style={{ margin: '16px 0' }} />
                <Button
                    type="primary"
                    ghost
                    icon={<AimOutlined />}
                    onClick={() => onViewAsCenter?.(selectedNode.uuid)}
                    block
                >
                    {intl.formatMessage({ id: 'lineage_view_as_center' })}
                </Button>
            </div>
        );
    };

    const renderEdgeDetail = () => {
        if (!selectedEdge) return null;
        const detail = selectedEdge.lineageDetail;
        const fromName = selectedEdge.fromEntity?.displayName || '';
        const toName = selectedEdge.toEntity?.displayName || '';
        const columnLineages = detail?.childRelDetailList || [];

        return (
            <div className="lineage-drawer">
                {/* Flow header */}
                <div style={{
                    display: 'flex', alignItems: 'center', gap: 12, marginBottom: 20,
                    padding: '12px 14px', background: '#f7f9ff', borderRadius: 8,
                }}
                >
                    <Text strong style={{ color: '#16a34a' }}>{fromName}</Text>
                    <ArrowRightOutlined style={{ color: '#9ca3af' }} />
                    <Text strong style={{ color: '#ea580c' }}>{toName}</Text>
                </div>

                {/* Source type */}
                {detail?.sourceType && (
                    <div className="drawer-section">
                        <div className="drawer-section-title">
                            {intl.formatMessage({ id: 'lineage_source_type' })}
                        </div>
                        <Tag color={SOURCE_TYPE_COLORS[detail.sourceType] || 'default'}>
                            {detail.sourceType}
                        </Tag>
                    </div>
                )}

                {/* Column lineage */}
                {columnLineages.length > 0 && (
                    <div className="drawer-section">
                        <div className="drawer-section-title">
                            {intl.formatMessage({ id: 'lineage_column_mapping' })}
                        </div>
                        <div style={{
                            border: '1px solid #e8ecf1', borderRadius: 8, overflow: 'hidden',
                        }}
                        >
                            {columnLineages.map((item, idx) => {
                                const fromCols = (item.fromChildren || []).map((c) => c.displayName).join(', ');
                                const toCol = item.toChild?.displayName || '';
                                return (
                                    <div
                                        key={idx}
                                        className="col-mapping-row"
                                        style={{
                                            padding: '8px 12px',
                                            background: idx % 2 === 0 ? '#fafbfe' : '#fff',
                                            borderBottom: idx < columnLineages.length - 1 ? '1px solid #f0f2f5' : 'none',
                                        }}
                                    >
                                        <Text code style={{ fontSize: 12 }}>{fromCols}</Text>
                                        <ArrowRightOutlined className="col-mapping-arrow" style={{ fontSize: 10 }} />
                                        <Text code style={{ fontSize: 12 }}>{toCol}</Text>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}

                {/* SQL */}
                {detail?.sqlQuery && (
                    <div className="drawer-section">
                        <div className="drawer-section-title">
                            {intl.formatMessage({ id: 'lineage_related_sql' })}
                        </div>
                        <div className="drawer-sql-block">
                            {detail.sqlQuery}
                        </div>
                    </div>
                )}

                <Divider style={{ margin: '16px 0' }} />
                <Popconfirm
                    title={intl.formatMessage({ id: 'lineage_delete_confirm' })}
                    onConfirm={() => {
                        const from = selectedEdge.fromEntity?.uuid;
                        const to = selectedEdge.toEntity?.uuid;
                        if (from && to) onDeleteEdge?.(from, to);
                    }}
                    okType="danger"
                >
                    <Button danger icon={<DeleteOutlined />} block>
                        {intl.formatMessage({ id: 'lineage_delete' })}
                    </Button>
                </Popconfirm>
            </div>
        );
    };

    const title = selectedNode
        ? intl.formatMessage({ id: 'lineage_view_detail' })
        : intl.formatMessage({ id: 'lineage_detail' });

    return (
        <Drawer
            title={title}
            placement="right"
            width={400}
            open={visible}
            onClose={onClose}
            destroyOnClose
            styles={{
                header: {
                    borderBottom: '1px solid #e8ecf1',
                    padding: '14px 20px',
                },
                body: {
                    padding: '16px 20px',
                },
            }}
        >
            {selectedNode ? renderNodeDetail() : renderEdgeDetail()}
        </Drawer>
    );
};

export default LineageDetailDrawer;
