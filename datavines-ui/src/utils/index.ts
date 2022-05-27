export function pickProps(source: Record<string, any>, props: string[]) {
    const target: Record<string, any> = {};
    props.forEach((propName) => {
        if (Object.prototype.hasOwnProperty.call(source, propName)) {
            target[propName] = source[propName];
        }
    });
    return target;
}
