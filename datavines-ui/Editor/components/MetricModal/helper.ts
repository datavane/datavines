export const captureName = (name: string) => {
    if (!name) {
        return '';
    }
    return name.substring(0, 1).toUpperCase() + name.substring(1);
};

export function pickProps(source: Record<string, any>, props: string[]) {
    const target: Record<string, any> = {};
    props.forEach((propName) => {
        if (Object.prototype.hasOwnProperty.call(source, propName)) {
            target[propName] = source[propName];
        }
    });
    return target;
}

export const layoutItem = {
    style: {
        marginBottom: 12,
    },
    labelCol: {
        span: 8,
    },
    wrapperCol: {
        span: 16,
    },
};
export const layoutActuatorItem = {
    style: {
        marginBottom: 12,
    },
    labelCol: {
        span: 12,
    },
    wrapperCol: {
        span: 12,
    },
};
export const layoutActuatorLineItem = {
    style: {
        marginBottom: 12,
    },
    labelCol: {
        span: 6,
    },
    wrapperCol: {
        span: 16,
    },
};

export const layoutOneLineItem = {
    style: {
        marginBottom: 12,
    },
    labelCol: {
        span: 3,
    },
    wrapperCol: {
        span: 21,
    },
};
