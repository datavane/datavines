import React, { useRef, useImperativeHandle, useState } from 'react';
import {
    Input, ModalProps, Radio, Space, InputNumber,
} from 'antd';
import {
    useModal, useImmutable, usePersistFn, IF, CustomSelect,
} from 'src/common';
import { useIntl } from 'react-intl';

type InnerProps = {
    innerRef: any
}

const minutesSource = [
    { label: '5', value: 5 },
    { label: '10', value: 10 },
    { label: '15', value: 15 },
    { label: '20', value: 20 },
    { label: '30', value: 30 },
];
const weekSource = [
    { label: 'Sunday', value: 'Sunday' },
    { label: 'Monday', value: 'Monday' },
    { label: 'Tuesday', value: 'Tuesday' },
    { label: 'Wednesday', value: 'Wednesday' },
    { label: 'Thursday', value: 'Thursday' },
    { label: 'Friday', value: 'Friday' },
    { label: 'Saturday', value: 'Saturday' },
];

const Inner = ({ innerRef }: InnerProps) => {
    const [type, setType] = useState<number | string>('Never');
    useImperativeHandle(innerRef, () => ({
        getData() {
            return { day: '' };
        },
    }));
    const onChange = (e: any) => {
        console.log('val', e.target.value);
        setType(e.target.value);
    };
    return (
        <div>
            <Radio.Group onChange={onChange} value={type}>
                <Space direction="vertical">
                    <Radio value="Never">Never</Radio>
                    <Radio value="Every N Minutes">
                        <div>Every N Minutes</div>
                        <IF visible={type === 'Every N Minutes'}>
                            Every
                            <CustomSelect
                                style={{
                                    display: 'inline-block',
                                    width: 70,
                                    margin: '0 5px',
                                }}
                                allowClear
                                placeholder="N"
                                source={minutesSource}
                            />
                            minutes,with first refresh
                            <InputNumber min={0} max={59} style={{ margin: '0 5px', width: 70 }} />
                            minutes after the hour
                        </IF>
                    </Radio>
                    <Radio value="Hourly">
                        <div>Hourly</div>
                        <IF visible={type === 'Hourly'}>
                            Every hour,at
                            <InputNumber min={0} max={59} style={{ margin: '0 5px', width: 70 }} />
                            minutes after the hour
                        </IF>
                    </Radio>
                    <Radio value="Every N hours">
                        <div>Every N hours</div>
                        <IF visible={type === 'Every N hours'}>
                            Every
                            <InputNumber min={0} max={59} style={{ margin: '0 5px', width: 70 }} />
                            hours,with first refresh each day at
                            <InputNumber min={0} max={59} style={{ margin: '0 2px', width: 50 }} />
                            :
                            <InputNumber min={0} max={59} style={{ margin: '0 5px', width: 50 }} />
                            UTC
                        </IF>
                    </Radio>
                    <Radio value="Daily">
                        <div>Daily</div>
                        <IF visible={type === 'Daily'}>
                            Every day, at
                            <InputNumber min={0} max={23} style={{ margin: '0 2px', width: 50 }} />
                            :
                            <InputNumber min={0} max={59} style={{ margin: '0 5px', width: 50 }} />
                            UTC
                        </IF>
                    </Radio>
                    <Radio value="Weekly">
                        <div>Weekly</div>
                        <IF visible={type === 'Weekly'}>
                            Every
                            <CustomSelect
                                style={{
                                    display: 'inline-block',
                                    width: 120,
                                    margin: '0 5px',
                                }}
                                allowClear
                                placeholder="Day of Week"
                                source={weekSource}
                            />
                            , at
                            <InputNumber min={0} max={59} style={{ margin: '0 2px', width: 50 }} />
                            :
                            <InputNumber min={0} max={59} style={{ margin: '0 5px', width: 50 }} />
                            UTC
                        </IF>
                    </Radio>
                    <Radio value="Cron Expression">
                        <div>Cron Expression</div>
                        <IF visible={type === 'Cron Expression'}>
                            <Input placeholder="0 0 * * *" style={{ display: 'inline-block', width: 150, marginRight: 5 }} />
                            At 12:00 AM(timezone;UTC)
                        </IF>
                    </Radio>
                </Space>
            </Radio.Group>
        </div>
    );
};

export const useScheduleModal = (options: ModalProps) => {
    const innerRef = useRef();
    const intl = useIntl();
    const onOk = usePersistFn(() => {
        console.log('connfirm ok');
    });
    const { Render, ...rest } = useModal<any>({
        title: 'Schedule Manage',
        width: 640,
        okText: intl.formatMessage({ id: 'common_update' }),
        ...(options || {}),
        onOk,
        // footer: null,
    });
    return {
        Render: useImmutable(() => (<Render><Inner innerRef={innerRef} /></Render>)),
        ...rest,
    };
};
