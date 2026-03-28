import G6 from '@antv/g6';
import { NODE_WIDTH, NODE_HEIGHT, ACCENT_BAR_WIDTH, COLORS } from './constants';

const DB_TYPE_ICONS: Record<string, string> = {
    mysql: '🐬', postgresql: '🐘', hive: '🐝', clickhouse: '⚡',
    oracle: '🔶', sqlserver: '🔷', presto: '◈', trino: '◈',
    spark: '✦', doris: '◆', starrocks: '★', impala: '▲',
};

function getTypeIcon(type: string): string {
    const key = (type || '').toLowerCase();
    return DB_TYPE_ICONS[key] || '⊡';
}

export function registerLineageNode() {
    G6.registerNode('lineage-table-node', {
        draw(cfg: any, group: any) {
            const nodeType = cfg.nodeType || 'upstream';
            const colors = COLORS[nodeType as keyof typeof COLORS] || COLORS.upstream;

            // Card body
            const shape = group.addShape('rect', {
                attrs: {
                    x: 0, y: 0,
                    width: NODE_WIDTH, height: NODE_HEIGHT,
                    radius: 8,
                    fill: colors.bg,
                    stroke: '#e8ecf1',
                    lineWidth: 1,
                    cursor: 'pointer',
                    shadowColor: 'rgba(0,0,0,0.06)',
                    shadowBlur: 12,
                    shadowOffsetY: 4,
                },
                name: 'node-rect',
            });

            // Left accent bar
            group.addShape('rect', {
                attrs: {
                    x: 0, y: 0,
                    width: ACCENT_BAR_WIDTH, height: NODE_HEIGHT,
                    radius: [8, 0, 0, 8],
                    fill: colors.accent,
                },
                name: 'accent-bar',
            });

            // Type icon circle
            const iconX = ACCENT_BAR_WIDTH + 16;
            const iconCY = NODE_HEIGHT / 2;
            group.addShape('circle', {
                attrs: {
                    x: iconX + 14, y: iconCY,
                    r: 16,
                    fill: colors.badge,
                    cursor: 'pointer',
                },
                name: 'icon-circle',
            });
            const dsType = cfg.datasourceType || '';
            group.addShape('text', {
                attrs: {
                    x: iconX + 14, y: iconCY + 1,
                    text: getTypeIcon(dsType),
                    fontSize: 15,
                    textAlign: 'center',
                    textBaseline: 'middle',
                    cursor: 'pointer',
                },
                name: 'icon-text',
            });

            // Text area starts after icon
            const textX = iconX + 40;
            const maxTextW = NODE_WIDTH - textX - 14;

            // Table name (primary)
            group.addShape('text', {
                attrs: {
                    x: textX, y: 28,
                    text: truncateText(cfg.label as string || 'Untitled', Math.floor(maxTextW / 7.5)),
                    fontSize: 13,
                    fontWeight: 600,
                    fill: colors.title,
                    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
                    cursor: 'pointer',
                    textBaseline: 'middle',
                },
                name: 'table-label',
            });

            // Database · Datasource (secondary line)
            const dbName = cfg.databaseName || '';
            const dsLabel = cfg.datasourceName || '';
            const subtitle = [dbName, dsLabel].filter(Boolean).join(' · ');
            group.addShape('text', {
                attrs: {
                    x: textX, y: 48,
                    text: truncateText(subtitle, Math.floor(maxTextW / 6.5)),
                    fontSize: 11,
                    fill: colors.subtitle,
                    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
                    cursor: 'pointer',
                    textBaseline: 'middle',
                },
                name: 'subtitle-label',
            });

            // Column count badge
            const colCount = cfg.columnCount || 0;
            if (colCount > 0) {
                const badgeText = `${colCount} cols`;
                const badgeW = badgeText.length * 7 + 12;
                group.addShape('rect', {
                    attrs: {
                        x: textX, y: 57,
                        width: badgeW, height: 18,
                        radius: 9,
                        fill: colors.badge,
                    },
                    name: 'col-badge-bg',
                });
                group.addShape('text', {
                    attrs: {
                        x: textX + badgeW / 2, y: 66,
                        text: badgeText,
                        fontSize: 10,
                        fill: colors.badgeText,
                        fontWeight: 500,
                        textAlign: 'center',
                        textBaseline: 'middle',
                        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
                    },
                    name: 'col-badge-text',
                });
            }

            // Expand arrow for nodes with deeper lineage
            if (cfg.hasNextNode) {
                const arrowX = NODE_WIDTH - 18;
                const arrowY = NODE_HEIGHT / 2;
                group.addShape('circle', {
                    attrs: {
                        x: arrowX, y: arrowY, r: 10,
                        fill: colors.badge,
                        cursor: 'pointer',
                    },
                    name: 'expand-bg',
                });
                group.addShape('text', {
                    attrs: {
                        x: arrowX, y: arrowY + 1,
                        text: '›',
                        fontSize: 16,
                        fontWeight: 700,
                        fill: colors.accent,
                        textAlign: 'center',
                        textBaseline: 'middle',
                        cursor: 'pointer',
                    },
                    name: 'expand-icon',
                });
            }

            // Current node indicator dot
            if (nodeType === 'current') {
                group.addShape('circle', {
                    attrs: {
                        x: NODE_WIDTH - 14, y: 14,
                        r: 4,
                        fill: colors.accent,
                    },
                    name: 'current-dot',
                });
            }

            return shape;
        },
        setState(name: string | undefined, value: string | boolean | undefined, item: any) {
            const group = item.getContainer();
            const rect = group.find((e: any) => e.get('name') === 'node-rect');
            if (!rect) return;
            const model = item.getModel();
            const nodeType = model.nodeType || 'upstream';
            const colors = COLORS[nodeType as keyof typeof COLORS] || COLORS.upstream;

            if (name === 'selected') {
                rect.attr({
                    stroke: value ? colors.accent : '#e8ecf1',
                    lineWidth: value ? 2 : 1,
                    shadowBlur: value ? 20 : 12,
                    shadowColor: value ? `${colors.accent}30` : 'rgba(0,0,0,0.06)',
                });
            }
            if (name === 'hover') {
                rect.attr({
                    fill: value ? colors.hoverBg : colors.bg,
                    shadowBlur: value ? 18 : 12,
                });
            }
        },
        getAnchorPoints() {
            return [
                [0, 0.5],
                [1, 0.5],
            ];
        },
    }, 'rect');
}

function truncateText(text: string, maxLen: number): string {
    if (text.length <= maxLen) return text;
    return `${text.slice(0, maxLen - 1)}…`;
}
