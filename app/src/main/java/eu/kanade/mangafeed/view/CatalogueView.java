package eu.kanade.mangafeed.view;

import uk.co.ribot.easyadapter.EasyAdapter;

public interface CatalogueView extends BaseView {
    void setAdapter(EasyAdapter adapter);
    void setSourceClickListener();
}