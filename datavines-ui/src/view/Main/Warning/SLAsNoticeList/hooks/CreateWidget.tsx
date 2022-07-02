import React, { useState } from 'react';
import {
    Input, ModalProps, Form, FormInstance, Radio, message,
} from 'antd';
import { useIntl } from 'react-intl';
import {
    useModal, useImmutable, FormRender, IFormRenderItem, IFormRender, usePersistFn, useMount, CustomSelect, useLoading,
} from '@/common';
import { $http } from '@/http';
import { useSelector } from '@/store';
import { NoticeDynamicItem } from '@/type/Notification';
import { pickProps } from '@/utils';

type InnerProps = {
    form: FormInstance | undefined
}

const Inner = ({ form }: InnerProps) => {
    const intl = useIntl();
    const [typeSource, setTypeSource] = useState<{label: string, value: string}[]>([]);
    const [dynamicMeta, setDynamicMeta] = useState<IFormRenderItem[]>([]);
    useMount(async () => {
        try {
            const res: string[] = (await $http.get('/sla/plugin/support')) || [];
            const data = res.map((item) => ({
                label: item,
                value: item,
            }));
            setTypeSource(data);
        } catch (error) {
        }
    });
    const typeChange = async (type: string) => {
        try {
            const res = (await $http.get(`/sla/sender/config/${type}`)) || [];
            if (res) {
                const data = JSON.parse(res) as NoticeDynamicItem[];
                setDynamicMeta(data.map((item) => {
                    const object = {
                        label: item.title,
                        name: item.field,
                        initialValue: item.value || undefined,
                        rules: (item.validate || []).map(($item) => (pickProps($item, ['message', 'required']))),
                    };
                    if (item.type === 'radio') {
                        return {
                            ...object,
                            widget: (
                                <Radio.Group>
                                    {
                                        (item.options || []).map((sub) => <Radio key={`${sub.value}`} value={sub.value} disabled={sub.disabled}>{sub.label}</Radio>)
                                    }
                                </Radio.Group>
                            ),
                        };
                    }
                    return {
                        ...object,
                        widget: <Input />,
                    };
                }));
            }
        } catch (error) {
        }
    };
    const schema: IFormRender = {
        name: 'notice-form',
        labelCol: { span: 8 },
        wrapperCol: { span: 16 },
        formItemProps: {
            style: { marginBottom: 10 },
        },
        meta: [
            {
                label: intl.formatMessage({ id: 'warn_SLAs_name' }),
                name: 'type',
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required_tip' }),
                    },
                ],
                widget: <CustomSelect
                    onChange={typeChange}
                    source={typeSource}
                    style={{ width: 200 }}
                />,
            },
            ...dynamicMeta,
        ],
    };
    return <FormRender {...schema} form={form} />;
};

export const useCreateWidget = (options: ModalProps) => {
    const [form] = Form.useForm();
    const intl = useIntl();
    const setLoading = useLoading();
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const onOk = usePersistFn(async () => {
        form.validateFields().then(async (values) => {
            console.log('values', values);
            try {
                setLoading(true);
                const params = {
                    workSpaceId: workspaceId,
                    ...values,
                };
                await $http.post('/sla/sender', params);
                message.success(intl.formatMessage({ id: 'common_success' }));
                hide();
            } catch (error) {
            } finally {
                setLoading(false);
            }
        }).catch((err) => {
            console.log(err);
        });
    });
    const { Render, hide, ...rest } = useModal<any>({
        title: intl.formatMessage({ id: 'warn_create_widget' }),
        onOk,
        width: 600,
        ...(options || {}),
    });
    return {
        Render: useImmutable(() => (<Render><Inner form={form} /></Render>)),
        ...rest,
    };
};
