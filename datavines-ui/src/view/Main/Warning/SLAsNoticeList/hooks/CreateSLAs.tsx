import React, { useRef } from 'react';
import {
    Input, ModalProps, Form, FormInstance, message,
} from 'antd';
import { useIntl } from 'react-intl';
import {
    useModal, useImmutable, FormRender, IFormRender, usePersistFn, useLoading, useContextModal,
} from '@/common';
import { $http } from '@/http';
import { useSelector } from '@/store';
import { TWarnSLATableItem } from '@/type/warning';

type InnerProps = {
    form: FormInstance | undefined
}

const Inner = ({ form }: InnerProps) => {
    const intl = useIntl();
    const { data } = useContextModal<TWarnSLATableItem>();
    console.log('data', data);
    const schema: IFormRender = {
        name: 'sla-form',
        labelCol: { span: 6 },
        wrapperCol: { span: 18 },
        onFinish(values) {
            console.log('yes', values);
        },
        formItemProps: {
            style: { marginBottom: 10 },
        },
        meta: [
            {
                label: intl.formatMessage({ id: 'warn_SLAs_name' }),
                name: 'name',
                initialValue: data?.name,
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required_tip' }),
                    },
                ],
                widget: <Input />,
            },
            {
                label: intl.formatMessage({ id: 'common_desc' }),
                name: 'description',
                initialValue: data?.description,
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required_tip' }),
                    },
                ],
                widget: <Input />,
            },
        ],
    };
    return <FormRender {...schema} form={form} />;
};

export const useCreateSLAs = (options: ModalProps) => {
    const [form] = Form.useForm();
    const intl = useIntl();
    const setLoading = useLoading();
    const editRef = useRef<TWarnSLATableItem | null>(null);
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
                if (editRef.current) {
                    await $http.put('/sla', { ...params, id: editRef.current.id });
                } else {
                    await $http.post('/sla', params);
                }
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
    const {
        Render, hide, show, ...rest
    } = useModal<any>({
        title: intl.formatMessage({ id: 'warn_create_SLAs' }),
        onOk,
        ...(options || {}),
    });
    return {
        Render: useImmutable(() => (<Render><Inner form={form} /></Render>)),
        show(data: TWarnSLATableItem | null) {
            editRef.current = data;
            show(data);
        },
        ...rest,
    };
};
