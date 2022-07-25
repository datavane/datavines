/* eslint-disable jsx-a11y/label-has-associated-control */
/* eslint-disable react/no-unstable-nested-components */
import React, { useRef, useImperativeHandle, useState } from 'react';
import { useIntl } from 'react-intl';
import {
    Input, InputNumber, Form, Radio, DatePicker, Col, Row, Button, FormInstance, message, Spin,
} from 'antd';
import useRequiredRule from '@Editor/hooks/useRequiredRule';
import moment from 'moment';
import { CustomSelect, useMount, useLoading } from '@/common';
import PageContainer from '../../useAddEditJobsModal/PageContainer';
import { pickProps } from '@/utils';
import { $http } from '@/http';

const get100Years = () => {
    const year = moment().year();
    return moment().set('year', year + 100);
};
type TParam = {
    cycle: string,
    crontab: null | string,
    parameter?: {
        minute: number,
        nminute: number,
        hour: number,
        nhour: number,
        wday: number,
        day: number,
    }
}
type TDetail = {
    id: number;
    jobId: number;
    type: string;
    param?: TParam;
    cronExpression: string;
    status: boolean;
    startTime: string;
    endTime: string;
    createBy: number;
    createTime: Date;
    updateBy: number;
    updateTime: Date;
}
type ScheduleProps = {
    formRef: { current: FormInstance},
    detail: null | TDetail,
}
const commonStyle = {
    display: 'inline-block',
    lineHeight: '32px',
};
const Schedule: React.FC<ScheduleProps> = ({ formRef, detail }) => {
    const intl = useIntl();
    const form = Form.useForm()[0];
    const requiredRule = useRequiredRule();
    const getIntl = (id: any) => intl.formatMessage({ id });
    useImperativeHandle(formRef, () => form);
    const cycleSource = [
        { label: getIntl('jobs_schedule_cycle_month'), value: 'month' },
        { label: getIntl('jobs_schedule_cycle_week'), value: 'week' },
        { label: getIntl('jobs_schedule_cycle_day'), value: 'day' },
        { label: getIntl('jobs_schedule_cycle_hour'), value: 'hour' },
        { label: getIntl('jobs_schedule_cycle_nhour'), value: 'nhour' },
        { label: getIntl('jobs_schedule_cycle_nminute'), value: 'nminute' },
    ];
    const WeekSrouce = [
        { label: getIntl('jobs_Monday'), value: 1, key: 'week_1' },
        { label: getIntl('jobs_Tuesday'), value: 2, key: 'week_2' },
        { label: getIntl('jobs_Wednesday'), value: 3, key: 'week_3' },
        { label: getIntl('jobs_Thursday'), value: 4, key: 'week_4' },
        { label: getIntl('jobs_Friday'), value: 5, key: 'week_5' },
        { label: getIntl('jobs_Saturday'), value: 6, key: 'week_6' },
        { label: getIntl('jobs_Sunday'), value: 0, key: 'week_0' },
    ];
    // @ts-ignore
    const Date = <DatePicker showTime />;
    useMount(() => {});
    const timeMap = useRef({
        minute: () => (
            <Form.Item
                label=""
                name="minute"
                rules={requiredRule}
                initialValue={detail?.param?.parameter?.minute}
                style={{ width: 60, display: 'inline-block' }}
            >
                <InputNumber
                    min={0}
                    max={59}
                    style={{ width: 60 }}
                />
            </Form.Item>
        ),
        nminute: () => (
            <Form.Item
                label=""
                name="nminute"
                rules={requiredRule}
                initialValue={detail?.param?.parameter?.nminute}
                style={{ width: 60, display: 'inline-block' }}
            >
                <InputNumber
                    min={0}
                    max={59}
                    style={{ width: 60 }}
                />
            </Form.Item>
        ),
        hour: () => (
            <Form.Item
                label=""
                name="hour"
                rules={requiredRule}
                initialValue={detail?.param?.parameter?.hour}
                style={{ width: 60, display: 'inline-block' }}
            >
                <InputNumber
                    min={0}
                    max={23}
                    style={{ width: 60 }}
                    // placeholder={intl.formatMessage({ id: 'jobs_schedule_cycle_hour' })}
                />
            </Form.Item>
        ),
        nhour: () => (
            <Form.Item
                label=""
                name="nhour"
                rules={requiredRule}
                initialValue={detail?.param?.parameter?.nhour}
                style={{ width: 60, display: 'inline-block' }}
            >
                <InputNumber
                    min={0}
                    max={23}
                    style={{ width: 60 }}
                    // placeholder={intl.formatMessage({ id: 'jobs_schedule_cycle_nhour' })}
                />
            </Form.Item>
        ),
        wday: () => (
            <Form.Item
                label=""
                name="wday"
                rules={requiredRule}
                initialValue={detail?.param?.parameter?.wday}
                style={{ width: 80, display: 'inline-block' }}
            >
                <CustomSelect source={WeekSrouce} style={{ width: 80 }} />
            </Form.Item>
        ),
        day: () => (
            <Form.Item
                label=""
                name="day"
                rules={requiredRule}
                initialValue={detail?.param?.parameter?.day}
                style={{ width: 100, display: 'inline-block' }}
            >
                <InputNumber
                    min={1}
                    max={31}
                    style={{ width: 100 }}
                    formatter={(value: any) => (value ? `${getIntl('jobs_every_month')}${value}${getIntl('jobs_every_month_day')}` : '')}
                    parser={(value: any) => (value || '').replace(getIntl('jobs_every_month'), '').replace(getIntl('jobs_every_month_day'), '')}
                    // placeholder={intl.formatMessage({ id: 'jobs_schedule_cycle_day' })}
                />
            </Form.Item>
        ),
    }).current;
    const renderScheduleTime = (cycleValue: string) => {
        switch (cycleValue) {
            case 'month':
                return (
                    <>
                        {timeMap.day()}
                        {timeMap.hour()}
                        <span style={{ ...commonStyle, margin: '0 5px' }}>:</span>
                        {timeMap.minute()}
                        <span style={{ ...commonStyle, marginLeft: 5 }}>{getIntl('jobs_week_after')}</span>
                    </>
                );
            case 'week':
                return (
                    <>
                        <span style={{ ...commonStyle, marginRight: 5 }}>{getIntl('jobs_week_before')}</span>
                        {timeMap.wday()}
                        {timeMap.hour()}
                        <span style={{ ...commonStyle, margin: '0 5px' }}>:</span>
                        {timeMap.minute()}
                        <span style={{ ...commonStyle, marginLeft: 5 }}>{getIntl('jobs_week_after')}</span>
                    </>
                );
            case 'day':
                return (
                    <>
                        <span style={{ ...commonStyle, marginRight: 5 }}>{getIntl('jobs_day_before')}</span>
                        {timeMap.hour()}
                        <span style={{ ...commonStyle, margin: '0 5px' }}>:</span>
                        {timeMap.minute()}
                        <span style={{ ...commonStyle, marginLeft: 5 }}>{getIntl('jobs_day_after')}</span>
                    </>
                );
            case 'nhour':
                return (
                    <>
                        <span style={{ ...commonStyle, marginRight: 5 }}>{getIntl('jobs_nhour_before')}</span>
                        {timeMap.nhour()}
                        <span style={{ ...commonStyle, margin: '0 5px' }}>:</span>
                        {timeMap.hour()}
                        <span style={{ ...commonStyle, marginRight: 5 }}>{getIntl('jobs_nhour_middle')}</span>
                        {timeMap.minute()}
                        <span style={{ ...commonStyle, marginLeft: 5 }}>{getIntl('jobs_nhour_after')}</span>
                    </>
                );
            case 'hour':
                return (
                    <>
                        <span style={{ ...commonStyle, marginRight: 5 }}>{getIntl('jobs_hour_before')}</span>
                        {timeMap.minute()}
                        <span style={{ ...commonStyle, marginLeft: 5 }}>{getIntl('jobs_hour_after')}</span>
                    </>
                );
            case 'nminute':
                return (
                    <>
                        <span style={{ ...commonStyle, marginRight: 5 }}>{getIntl('jobs_nminute_before')}</span>
                        {timeMap.nminute()}
                        <span style={{ ...commonStyle, margin: '0 5px' }}>{getIntl('jobs_nminute_middle')}</span>
                        {timeMap.minute()}
                        <span style={{ ...commonStyle, marginLeft: 5 }}>{getIntl('jobs_nminute_after')}</span>
                    </>
                );
            default:
                return null;
        }
    };
    return (
        <Form form={form}>
            <Form.Item
                label={intl.formatMessage({ id: 'jobs_schedule_type' })}
                name="type"
                rules={requiredRule}
                initialValue={detail?.type}
            >
                <Radio.Group>
                    <Radio value="cycle">{intl.formatMessage({ id: 'jobs_schedule_custom' })}</Radio>
                    <Radio value="cron">{intl.formatMessage({ id: 'jobs_schedule_cronbtab' })}</Radio>
                    <Radio value="offline">{intl.formatMessage({ id: 'jobs_schedule_offline' })}</Radio>
                </Radio.Group>
            </Form.Item>
            <Form.Item noStyle dependencies={['type']}>
                {() => {
                    const value = form.getFieldValue('type');
                    if (value !== 'cycle') {
                        return null;
                    }
                    return (
                        <>
                            <Form.Item
                                label={intl.formatMessage({ id: 'jobs_schedule_cycle' })}
                                name="cycle"
                                rules={requiredRule}
                                initialValue={detail?.param?.cycle}
                            >
                                <CustomSelect source={cycleSource} style={{ width: 240 }} />
                            </Form.Item>
                            <Form.Item noStyle dependencies={['cycle']}>
                                {() => {
                                    const cycleValue = form.getFieldValue('cycle');
                                    return (
                                        <Row>
                                            <div className="ant-col ant-form-item-label">
                                                <label className="ant-form-item-required">
                                                    {intl.formatMessage({ id: 'jobs_schedule_time' })}
                                                </label>
                                            </div>
                                            {renderScheduleTime(cycleValue)}
                                        </Row>
                                    );
                                }}
                            </Form.Item>

                            <Form.Item
                                label={<span style={{ marginLeft: 11 }}>{intl.formatMessage({ id: 'jobs_schedule_express' })}</span>}
                                name=" "
                                initialValue={undefined}
                            >
                                <div style={{ color: '#ff4d4f' }}>{detail?.cronExpression || intl.formatMessage({ id: 'jobs_schedule_auto_generage' })}</div>
                            </Form.Item>
                        </>
                    );
                }}
            </Form.Item>
            <Form.Item noStyle dependencies={['type']}>
                {() => {
                    const value = form.getFieldValue('type');
                    if (value === 'cron') {
                        return (
                            <Form.Item
                                label={intl.formatMessage({ id: 'jobs_schedule_express' })}
                                name="crontab"
                                rules={requiredRule}
                                initialValue={detail?.param?.crontab}
                            >
                                <Input style={{ width: 240 }} />
                            </Form.Item>
                        );
                    }
                    return null;
                }}
            </Form.Item>
            <Form.Item noStyle dependencies={['type']}>
                {() => {
                    const value = form.getFieldValue('type');
                    if (value === 'offline' || !value) {
                        return null;
                    }
                    return (
                        <Row>
                            <Col style={{ display: 'inline-block' }}>
                                <Form.Item
                                    label={intl.formatMessage({ id: 'jobs_schedule_obtain_time' })}
                                    name="startTime"
                                    initialValue={detail?.startTime ? moment(detail?.startTime) : moment()}
                                    rules={requiredRule}
                                >
                                    {Date}
                                </Form.Item>

                            </Col>
                            <span style={{ margin: '5px 10px 0px' }}>
                                {getIntl('jobs_schedule_time_to')}
                            </span>
                            <Form.Item
                                label=""
                                name="endTime"
                                initialValue={detail?.endTime ? moment(detail?.endTime) : get100Years()}
                                rules={requiredRule}
                                style={{ display: 'inline-block' }}
                            >
                                {Date}
                            </Form.Item>
                        </Row>
                    );
                }}
            </Form.Item>

        </Form>
    );
};
const ScheduleContainer = ({ jobId }: {jobId: string}) => {
    const intl = useIntl();
    const globalSetLoading = useLoading();
    const [loading, setLoading] = useState(true);
    const [detail, setDetail] = useState<TDetail | null>(null);
    const formRef = React.useRef() as { current: FormInstance};
    const getValues = (callback: (...args: any[]) => any) => {
        formRef.current.validateFields().then(async (values) => {
            const params: any = {
                type: values.type,
            };
            if (values.type !== 'offline') {
                params.startTime = moment(values.startTime).format('YYYY-MM-DD HH:mm:ss');
                params.endTime = moment(values.endTime).format('YYYY-MM-DD HH:mm:ss');
                params.param = {};
            }
            if (values.type === 'cron') {
                params.param.crontab = values.crontab;
            }
            if (values.type === 'cycle') {
                params.param = {
                    cycle: values.cycle,
                    parameter: pickProps(values, ['minute', 'nminute', 'hour', 'nhour', 'wday', 'day']),
                };
            }
            if (callback) {
                callback(params);
            }
        }).catch((err) => {
            console.log(err);
        });
    };
    const getData = async () => {
        try {
            const res = await $http.get(`/job/schedule/${jobId}`);
            if (res) {
                if (res.param) {
                    res.param = JSON.parse(res.param);
                }
                setDetail(res);
            }
        } catch (error) {
        } finally {
            setLoading(false);
        }
    };
    const onSave = () => {
        getValues(async (params: any) => {
            globalSetLoading(true);
            try {
                const $params = {
                    jobId,
                    ...params,
                };
                if (detail?.id) {
                    $params.id = detail?.id;
                }
                await $http.post('/job/schedule/createOrUpdate', $params);
                getData();
                message.success(intl.formatMessage({ id: 'common_success' }));
            } catch (error) {
            } finally {
                globalSetLoading(false);
            }
        });
    };
    useMount(async () => {
        getData();
    });
    if (loading) {
        return <Spin spinning={loading} />;
    }
    return (
        <PageContainer
            footer={(
                <Button type="primary" onClick={() => onSave()}>保存</Button>
            )}
        >
            <div style={{ width: 'calc(100vw - 80px)' }}>
                <Schedule formRef={formRef} detail={detail} />
            </div>
        </PageContainer>
    );
};

export default ScheduleContainer;
