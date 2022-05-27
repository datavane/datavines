import React, { useState, useRef } from 'react';
import {
    Input, ModalProps, Form, FormInstance, Button, message,
} from 'antd';
import {
    useModal, useImmutable, FormRender, IFormRenderItem, IFormRender, CustomSelect, usePersistFn, useLoading, useContextModal, useMount,
} from 'src/common';
import { useIntl } from 'react-intl';
import { ICreateDataSourceItem, IDataSourceListItem } from '@/type/dataSource';
import { pickProps } from '@/utils';
import { $http } from '@/http';
import { useSelector } from '@/store';

type InnerProps = {
    form: FormInstance | undefined
}

const Inner = ({ form }: InnerProps) => {
    const intl = useIntl();
    const { data: initData } = useContextModal<IDataSourceListItem | null>();
    const [dynamicMeta, setDynamicMeta] = useState<IFormRenderItem[]>([]);
    const onSourceTypeChange = async (type: string) => {
        try {
            const res = (await $http.get(`/datasource/config/${type}`) || '[]');
            const array = (JSON.parse(res) || []) as ICreateDataSourceItem[];
            setDynamicMeta(array.map((item) => {
                const isTextarea = item.type === 'input' && item.props?.type === 'textarea';
                const $props = pickProps(item.props || {}, ['placeholder', isTextarea && 'rows', 'disabled', 'size'].filter(Boolean) as string[]);
                return {
                    label: item.title,
                    name: item.field,
                    rules: (item.validate || []).map(($item) => (pickProps($item, ['message', 'required']))),
                    widget: isTextarea ? <Input.TextArea {...$props} /> : <Input {...$props} />,
                };
            }));
        } catch (error) {

        }
    };
    useMount(async () => {
        if (initData) {
            await onSourceTypeChange(initData.type);
            const paramObj = JSON.parse(initData.param || '{}');
            form?.setFieldsValue({
                name: initData.name,
                type: initData.type,
                ...paramObj,
            });
        }
    });
    const tipText = intl.formatMessage({ id: 'common_input_tip' });
    const nameText = intl.formatMessage({ id: 'datasource_modal_name' });
    const sourceTypeText = intl.formatMessage({ id: 'datasource_modal_source_type' });
    const schema: IFormRender = {
        name: 'userForm',
        layout: 'vertical',
        column: 1,
        gutter: 20,
        onFinish(values) {
            console.log('yes', values);
        },
        formItemProps: {
            style: { marginBottom: 10 },
        },
        meta: [
            {
                label: nameText,
                name: 'name',
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required' }),
                    },
                ],
                initialValue: initData?.name,
                widget: <Input size="small" placeholder={`${tipText}${nameText}`} />,
            },
            {
                label: sourceTypeText,
                name: 'type',
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required' }),
                    },
                ],
                initialValue: initData?.type,
                widget: <CustomSelect size="small" placeholder={`${tipText}${sourceTypeText}`} onChange={onSourceTypeChange} source={[{ label: 'mysql', value: 'mysql' }]} />,
            },
            ...dynamicMeta,
        ],
    };
    return <FormRender {...schema} form={form} />;
};

export const useAddDataSource = (options: ModalProps) => {
    const [form] = Form.useForm() as [FormInstance];
    const intl = useIntl();
    const setLoading = useLoading();
    const { workspaceId } = useSelector((r) => r.workSpaceReducer);
    const initDataRef = useRef<IDataSourceListItem | null>(null);
    const onConfirm = usePersistFn(() => {
        form.validateFields().then(async (values) => {
            try {
                setLoading(true);
                const { name, type, ...rest } = values;
                if (initDataRef.current?.id) {
                    await $http.put('/datasource', {
                        id: initDataRef.current?.id,
                        name,
                        type,
                        workspaceId,
                        param: JSON.stringify(rest),
                    });
                } else {
                    await $http.post('/datasource', {
                        name,
                        type,
                        workspaceId,
                        param: JSON.stringify(rest),
                    });
                }
                hide();
            } catch (error: any) {
            } finally {
                setLoading(false);
            }
        }).catch(() => {});
    });
    const onTestLink = usePersistFn(() => {
        form.validateFields().then(async (values) => {
            try {
                setLoading(true);
                // eslint-disable-next-line @typescript-eslint/no-unused-vars
                const { name, type, ...rest } = values;
                await $http.post('/datasource/test', {
                    type,
                    dataSourceParam: JSON.stringify(rest),
                });
                message.success('Success!');
            } catch (error: any) {
            } finally {
                setLoading(false);
            }
        }).catch(() => {});
    });
    const {
        Render, hide, show, ...rest
    } = useModal<any>({
        title: '',
        width: 640,
        ...(options || {}),
        footer: (
            <div style={{ textAlign: 'center' }}>
                <Button style={{ width: 120 }} onClick={onTestLink}>{intl.formatMessage({ id: 'test_link' })}</Button>
                <Button
                    style={{ width: 120 }}
                    type="primary"
                    onClick={onConfirm}
                >
                    {intl.formatMessage({ id: 'confirm_text' })}
                </Button>
            </div>
        ),
    });
    return {
        hide,
        show(data: IDataSourceListItem | null) {
            initDataRef.current = data;
            show(data);
        },
        Render: useImmutable(() => (<Render><Inner form={form} /></Render>)),
        ...rest,
    };
};
