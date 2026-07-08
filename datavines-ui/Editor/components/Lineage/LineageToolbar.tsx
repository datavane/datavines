import React from 'react';
import { Input, Segmented, Button, Tooltip, Space } from 'antd';
import {
    ReloadOutlined, PlusOutlined, CodeOutlined,
} from '@ant-design/icons';
import { useIntl } from 'react-intl';

interface LineageToolbarProps {
    onRefresh: () => void;
    onFitView: () => void;
    onAddLineage: () => void;
    onParseSql: () => void;
    direction: string;
    onDirectionChange: (dir: string) => void;
    onSearch: (value: string) => void;
}

const LineageToolbar: React.FC<LineageToolbarProps> = ({
    onRefresh, onAddLineage, onParseSql,
    direction, onDirectionChange, onSearch,
}) => {
    const intl = useIntl();
    return (
        <div className="lineage-toolbar">
            <div className="toolbar-left">
                <Input.Search
                    placeholder={intl.formatMessage({ id: 'lineage_search_placeholder' })}
                    style={{ width: 220 }}
                    allowClear
                    onSearch={onSearch}
                    size="middle"
                />
                <Segmented
                    value={direction}
                    onChange={(val) => onDirectionChange(val as string)}
                    options={[
                        { value: 'all', label: intl.formatMessage({ id: 'lineage_both' }) },
                        { value: 'upstream', label: intl.formatMessage({ id: 'lineage_upstream' }) },
                        { value: 'downstream', label: intl.formatMessage({ id: 'lineage_downstream' }) },
                    ]}
                    size="middle"
                />
            </div>
            <div className="toolbar-right">
                <Space size={4}>
                    <Tooltip title={intl.formatMessage({ id: 'lineage_refresh' })}>
                        <Button icon={<ReloadOutlined />} onClick={onRefresh} />
                    </Tooltip>
                    <Tooltip title={intl.formatMessage({ id: 'lineage_add' })}>
                        <Button type="primary" icon={<PlusOutlined />} onClick={onAddLineage}>
                            {intl.formatMessage({ id: 'lineage_add' })}
                        </Button>
                    </Tooltip>
                    <Tooltip title={intl.formatMessage({ id: 'lineage_add_by_sql' })}>
                        <Button icon={<CodeOutlined />} onClick={onParseSql}>
                            {intl.formatMessage({ id: 'lineage_add_by_sql' })}
                        </Button>
                    </Tooltip>
                </Space>
            </div>
        </div>
    );
};

export default LineageToolbar;
