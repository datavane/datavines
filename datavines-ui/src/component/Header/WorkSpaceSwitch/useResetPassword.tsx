import React, { useState, useRef } from 'react';
import {
    Input, ModalProps, Form, FormInstance,
} from 'antd';
import {
    useModal, useImmutable, FormRender, IFormRender, usePersistFn, useContextModal,
} from 'src/common';
import { useIntl } from 'react-intl';
import { $http } from '@/http';

type InnerProps = {
    form: FormInstance | undefined
}

const Inner = ({ form }: InnerProps) => {
    const intl = useIntl();

    const schema: IFormRender = {
        name: 'reset-password',
        layout: 'vertical',
        meta: [
            {
                label: intl.formatMessage({ id: 'reset_password_old' }),
                name: 'oldPassword',
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required' }),
                    },
                ],
                widget: <Input autoComplete="off" />,
            },
            {
                label: intl.formatMessage({ id: 'reset_password_new' }),
                name: 'newPassword',
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required' }),
                    },
                ],
                widget: <Input autoComplete="off" />,
            },
            {
                label: intl.formatMessage({ id: 'reset_password_new_confirm' }),
                name: 'newPasswordConfirm',
                rules: [
                    {
                        required: true,
                        message: intl.formatMessage({ id: 'common_required' }),
                    },
                ],
                widget: <Input autoComplete="off" />,
            }
        ],
    };
    return <FormRender {...schema} form={form} />;
};

export const useResetPassword = (options: ModalProps) => {
    const [form] = Form.useForm() as [FormInstance];
    const [loading, setLoading] = useState(false);
    const loadingRef = useRef(loading);
    loadingRef.current = loading;
    const intl = useIntl();
    const userIdRef = useRef<number | null>();
    const onOk = usePersistFn(() => {
        form.validateFields().then(async (values) => {
            try {
                setLoading(true);
                await $http.post('/user/resetPassword',  { ...values, id: userIdRef.current });
                hide();
            } catch (error: any) {
            } finally {
                setLoading(false);
            }
        }).catch(() => {});
    });

    const {
        Render, hide, show, ...rest
    } = useModal<any>({
        title: intl.formatMessage({ id: 'common_reset_password' }),
        ...(options || {}),
        confirmLoading: loadingRef.current,
        onOk,
    });

    return {
        hide,
        show1(data?: number | null) {
            userIdRef.current = data;
            show(data);
        },
        Render: useImmutable(() => (<Render><Inner form={form} /></Render>)),
        ...rest,
    };
};
