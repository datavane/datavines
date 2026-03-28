import React, { useState, useEffect } from 'react';
import { Modal, Form, Select, Input, message, Alert } from 'antd';
import { useIntl } from 'react-intl';
import useRequest from '../../hooks/useRequest';

const { TextArea } = Input;

interface SqlParseModalProps {
    visible: boolean;
    onClose: () => void;
    onSuccess: () => void;
    datasourceList: any[];
}

const SqlParseModal: React.FC<SqlParseModalProps> = ({
    visible, onClose, onSuccess, datasourceList,
}) => {
    const intl = useIntl();
    const { $http } = useRequest();
    const [form] = Form.useForm();
    const [submitting, setSubmitting] = useState(false);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    const handleSubmit = async () => {
        try {
            const values = await form.validateFields();
            setSubmitting(true);
            setErrorMsg(null);

            const ds = datasourceList.find((d: any) => d.id === values.datasourceId);
            const payload = {
                dataSourceInfos: [{
                    id: values.datasourceId,
                    type: ds?.type || '',
                }],
                sql: values.sql,
            };

            await $http.post('/catalog/lineage/addByParseSql', payload, { hideError: true });
            message.success(intl.formatMessage({ id: 'lineage_add_by_sql' }) + ' ✓');
            form.resetFields();
            setErrorMsg(null);
            onSuccess();
        } catch (e: any) {
            const errText = e?.msg || e?.message || e?.data?.msg || 'Unknown error';
            setErrorMsg(errText);
        } finally {
            setSubmitting(false);
        }
    };

    useEffect(() => {
        if (!visible) {
            form.resetFields();
            setErrorMsg(null);
        }
    }, [visible]);

    return (
        <Modal
            title={intl.formatMessage({ id: 'lineage_add_by_sql' })}
            open={visible}
            onCancel={onClose}
            onOk={handleSubmit}
            confirmLoading={submitting}
            width={640}
            destroyOnClose
        >
            {errorMsg && (
                <Alert
                    type="error"
                    showIcon
                    closable
                    message={errorMsg}
                    style={{ marginBottom: 16 }}
                    onClose={() => setErrorMsg(null)}
                />
            )}
            <Form form={form} layout="vertical">
                <Form.Item
                    name="datasourceId"
                    label={intl.formatMessage({ id: 'lineage_select_datasource' })}
                    rules={[{ required: true }]}
                >
                    <Select
                        placeholder={intl.formatMessage({ id: 'lineage_select_datasource' })}
                        options={(datasourceList || []).map((ds: any) => ({
                            label: `${ds.name} (${ds.type})`, value: ds.id,
                        }))}
                        showSearch
                        filterOption={(input, option) =>
                            (option?.label as string)?.toLowerCase().includes(input.toLowerCase())
                        }
                    />
                </Form.Item>
                <Form.Item
                    name="sql"
                    label="SQL"
                    rules={[{ required: true }]}
                    extra="Example: INSERT INTO target_table SELECT * FROM source_table"
                >
                    <TextArea
                        rows={10}
                        placeholder="INSERT INTO target_table SELECT ... FROM source_table"
                        style={{ fontFamily: "'SF Mono', 'Fira Code', Consolas, monospace", fontSize: 13 }}
                    />
                </Form.Item>
            </Form>
        </Modal>
    );
};

export default SqlParseModal;
