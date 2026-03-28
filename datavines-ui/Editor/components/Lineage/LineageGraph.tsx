import React, { useRef, useEffect, useCallback } from 'react';
import G6, { Graph, IG6GraphEvent } from '@antv/g6';
import { Button, Tooltip } from 'antd';
import {
    ZoomInOutlined, ZoomOutOutlined, CompressOutlined,
} from '@ant-design/icons';
import { NODE_WIDTH, NODE_HEIGHT, EDGE_COLORS, EDGE_ACTIVE_COLORS } from './constants';
import { registerLineageNode } from './registerNodes';
import {
    CatalogEntityLineageVO, LineageEntityNodeInfo, LineageEntityEdgeInfo,
} from './useLineageData';
import './LineageGraph.less';

let nodesRegistered = false;

interface LineageGraphProps {
    data: CatalogEntityLineageVO | null;
    onNodeClick?: (node: LineageEntityNodeInfo) => void;
    onEdgeClick?: (edge: LineageEntityEdgeInfo) => void;
    onExpandNode?: (uuid: string) => void;
}

const LineageGraph: React.FC<LineageGraphProps> = ({
    data, onNodeClick, onEdgeClick, onExpandNode,
}) => {
    const containerRef = useRef<HTMLDivElement>(null);
    const graphRef = useRef<Graph | null>(null);

    const transformData = useCallback((lineageVO: CatalogEntityLineageVO) => {
        const currentUuid = lineageVO.currentNode?.uuid;
        const upstreamUuids = new Set<string>();
        const downstreamUuids = new Set<string>();

        (lineageVO.edges || []).forEach((edge) => {
            const fromUuid = edge.fromEntity?.uuid;
            const toUuid = edge.toEntity?.uuid;
            if (toUuid === currentUuid && fromUuid) upstreamUuids.add(fromUuid);
            if (fromUuid === currentUuid && toUuid) downstreamUuids.add(toUuid);
        });

        const nodes = (lineageVO.nodes || []).map((node) => {
            let nodeType = 'upstream';
            if (node.uuid === currentUuid) nodeType = 'current';
            else if (downstreamUuids.has(node.uuid)) nodeType = 'downstream';
            return {
                id: node.uuid,
                label: node.displayName || '',
                nodeType,
                datasourceName: node.datasource?.displayName || '',
                datasourceType: node.datasource?.type || '',
                databaseName: node.database?.displayName || '',
                columnCount: node.columns?.length || 0,
                hasNextNode: node.hasNextNode || false,
                type: 'lineage-table-node',
                rawData: node,
            };
        });

        const edges = (lineageVO.edges || []).map((edge) => {
            const fromUuid = edge.fromEntity?.uuid || '';
            const toUuid = edge.toEntity?.uuid || '';
            let colorKey: 'upstream' | 'downstream' | 'default' = 'default';
            if (toUuid === currentUuid) colorKey = 'upstream';
            else if (fromUuid === currentUuid) colorKey = 'downstream';

            return {
                id: edge.uuid || `${fromUuid}-${toUuid}`,
                source: fromUuid,
                target: toUuid,
                colorKey,
                style: {
                    stroke: EDGE_COLORS[colorKey],
                    lineWidth: 2,
                    endArrow: {
                        path: G6.Arrow.triangle(6, 6, 8),
                        fill: EDGE_COLORS[colorKey],
                        d: 8,
                    },
                    shadowColor: 'transparent',
                    shadowBlur: 0,
                },
                rawData: edge,
            };
        });

        return { nodes, edges };
    }, []);

    useEffect(() => {
        if (!containerRef.current || !data) return;

        if (!nodesRegistered) {
            registerLineageNode();
            nodesRegistered = true;
        }

        const container = containerRef.current;
        const width = container.clientWidth || 800;
        const height = container.clientHeight || 500;

        if (graphRef.current) {
            graphRef.current.destroy();
            graphRef.current = null;
        }

        try {
            const g6Data = transformData(data);
            if (!g6Data.nodes.length) return;

            const minimap = new G6.Minimap({
                size: [140, 90],
                className: 'lineage-minimap',
            });

            const graph = new G6.Graph({
                container,
                width,
                height,
                layout: {
                    type: 'dagre',
                    rankdir: 'LR',
                    nodesep: 50,
                    ranksep: 100,
                    align: 'UL',
                },
                defaultNode: {
                    type: 'lineage-table-node',
                    size: [NODE_WIDTH, NODE_HEIGHT],
                },
                defaultEdge: {
                    type: 'cubic-horizontal',
                    style: {
                        stroke: EDGE_COLORS.default,
                        lineWidth: 2,
                        lineDash: undefined,
                    },
                },
                modes: {
                    default: ['drag-canvas', 'zoom-canvas'],
                },
                plugins: [minimap],
                fitView: true,
                fitViewPadding: [60, 60, 60, 60],
                maxZoom: 1.5,
                minZoom: 0.15,
                animate: true,
                animateCfg: { duration: 350, easing: 'easeCubic' },
            });

            graph.on('node:click', (evt: IG6GraphEvent) => {
                const model = evt.item?.getModel();
                if (!model) return;
                const targetName = (evt.target as any)?.get?.('name');
                if ((targetName === 'expand-icon' || targetName === 'expand-bg') && onExpandNode) {
                    onExpandNode(model.id as string);
                    return;
                }
                // Clear previous selection
                graph.getNodes().forEach((n) => graph.setItemState(n, 'selected', false));
                graph.setItemState(evt.item!, 'selected', true);
                if (onNodeClick && model.rawData) {
                    onNodeClick(model.rawData as LineageEntityNodeInfo);
                }
            });

            graph.on('node:mouseenter', (evt: IG6GraphEvent) => {
                graph.setItemState(evt.item!, 'hover', true);
                container.style.cursor = 'pointer';
            });
            graph.on('node:mouseleave', (evt: IG6GraphEvent) => {
                graph.setItemState(evt.item!, 'hover', false);
                container.style.cursor = 'default';
            });

            graph.on('edge:click', (evt: IG6GraphEvent) => {
                const model = evt.item?.getModel();
                if (!model) return;
                if (onEdgeClick && model.rawData) {
                    onEdgeClick(model.rawData as LineageEntityEdgeInfo);
                }
            });

            graph.on('edge:mouseenter', (evt: IG6GraphEvent) => {
                const model = evt.item?.getModel();
                const colorKey = (model?.colorKey as string) || 'default';
                const activeColor = EDGE_ACTIVE_COLORS[colorKey as keyof typeof EDGE_ACTIVE_COLORS] || EDGE_ACTIVE_COLORS.default;
                graph.updateItem(evt.item!, {
                    style: {
                        stroke: activeColor,
                        lineWidth: 3,
                        shadowColor: `${activeColor}40`,
                        shadowBlur: 8,
                        endArrow: {
                            path: G6.Arrow.triangle(7, 7, 8),
                            fill: activeColor,
                            d: 8,
                        },
                    },
                });
                container.style.cursor = 'pointer';
            });

            graph.on('edge:mouseleave', (evt: IG6GraphEvent) => {
                const model = evt.item?.getModel();
                const colorKey = (model?.colorKey as string) || 'default';
                const edgeColor = EDGE_COLORS[colorKey as keyof typeof EDGE_COLORS] || EDGE_COLORS.default;
                graph.updateItem(evt.item!, {
                    style: {
                        stroke: edgeColor,
                        lineWidth: 2,
                        shadowColor: 'transparent',
                        shadowBlur: 0,
                        endArrow: {
                            path: G6.Arrow.triangle(6, 6, 8),
                            fill: edgeColor,
                            d: 8,
                        },
                    },
                });
                container.style.cursor = 'default';
            });

            graph.on('canvas:click', () => {
                graph.getNodes().forEach((n) => graph.setItemState(n, 'selected', false));
            });

            graph.data(g6Data);
            graph.render();
            graphRef.current = graph;

            const resizeObserver = new ResizeObserver(() => {
                if (graphRef.current && container) {
                    graphRef.current.changeSize(container.clientWidth, container.clientHeight);
                    graphRef.current.fitView(60);
                }
            });
            resizeObserver.observe(container);

            return () => {
                resizeObserver.disconnect();
                if (graphRef.current) {
                    graphRef.current.destroy();
                    graphRef.current = null;
                }
            };
        } catch (err) {
            console.error('[LineageGraph] render error:', err);
        }
    }, [data, onNodeClick, onEdgeClick, onExpandNode, transformData]);

    const handleZoom = (factor: number) => {
        if (!graphRef.current) return;
        const zoom = graphRef.current.getZoom();
        graphRef.current.zoomTo(Math.min(Math.max(zoom * factor, 0.15), 3), undefined, true, { duration: 200 });
    };

    const handleFitView = () => {
        graphRef.current?.fitView(60, undefined, true, { duration: 300 });
    };

    return (
        <div className="lineage-graph-container">
            <div className="lineage-canvas" ref={containerRef} />

            <div className="lineage-legend">
                <div className="legend-item">
                    <span className="legend-dot" style={{ background: '#4169E1' }} />
                    <span>Current</span>
                </div>
                <div className="legend-item">
                    <span className="legend-dot" style={{ background: '#16a34a' }} />
                    <span>Upstream</span>
                </div>
                <div className="legend-item">
                    <span className="legend-dot" style={{ background: '#ea580c' }} />
                    <span>Downstream</span>
                </div>
            </div>

            <div className="lineage-zoom-controls">
                <Tooltip title="Zoom In" placement="top">
                    <Button icon={<ZoomInOutlined />} onClick={() => handleZoom(1.25)} />
                </Tooltip>
                <span className="zoom-divider" />
                <Tooltip title="Zoom Out" placement="top">
                    <Button icon={<ZoomOutOutlined />} onClick={() => handleZoom(0.8)} />
                </Tooltip>
                <span className="zoom-divider" />
                <Tooltip title="Fit View" placement="top">
                    <Button icon={<CompressOutlined />} onClick={handleFitView} />
                </Tooltip>
            </div>
        </div>
    );
};

export default LineageGraph;
