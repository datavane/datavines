import React from 'react';
import useRoute from 'src/router/useRoute';
import { useIntl } from 'react-intl';
import { Route, Switch, useRouteMatch } from 'react-router-dom';
import { MenuItem } from '@/component/Menu/MenuAside';
import MenuLayout from '@/component/Menu/Layout';

const DetailMain = () => {
    const intl = useIntl();
    const match = useRouteMatch();
    const { detailRoutes, visible } = useRoute();
    if (!visible || !detailRoutes.length) {
        return null;
    }
    const detailMenus = detailRoutes.map((item) => ({
        ...item,
        key: item.path.replace(/:id/, (match.params as any).id || ''),
        label: intl.formatMessage({ id: item.path as any }),
    })) as MenuItem[];

    const generateRoute = (menusArray: MenuItem[]) => menusArray.map((route) => (
        <Route
            key={`${route.label}-${route.path}`}
            path={route.path}
            exact={route.exact ? true : undefined}
            component={route.component}
        />
    ));

    return (
        <MenuLayout menus={detailMenus}>
            <Switch>
                {generateRoute(detailMenus)}
            </Switch>
        </MenuLayout>
    );
};

export default DetailMain;
