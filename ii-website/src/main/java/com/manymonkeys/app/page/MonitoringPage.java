package com.manymonkeys.app.page;


import com.manymonkeys.spring.SpringContextHelper;
import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
import com.vaadin.ui.Table;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.index.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.Page;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Page
@Configurable(preConstruction = true)
public class MonitoringPage extends VerDashLayout {

//    @Autowired
//    IndexService indexService

//    @Autowired
//    GraphDatabaseService graphDb;

    @Override
    public void attach() {
        super.attach();

        //TODO: fix this after @Autowired gets fixed
//        SpringContextHelper helper = new SpringContextHelper(getApplication());
//        GraphDatabaseService graphDb = (GraphDatabaseService) helper.getBean("graphDbService");
//        IndexService indexService = (IndexService) helper.getBean("indexService");

        setMargin(true);

        Table table = new Table();
        addComponent(table);
        table.setSelectable(false);
        table.setWidth("100%");
        table.addContainerProperty("Parameter", String.class, "");
        table.addContainerProperty("Value", Object.class, null);
        table.setColumnExpandRatio(2, 1);

        Object[] item = {"Nodes in Graph Database", new Long(239)};
        table.addItem(item, 1);
    }
}
