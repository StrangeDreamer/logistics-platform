package cn.tycoding.entity.LogisticsPlatform;




import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.CanvasRenderer.AbstractCanvasRenderer;
import com.github.rinde.rinsim.ui.renderers.ViewPort;
import com.google.auto.value.AutoValue;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;

import java.util.Map;
import java.util.Map.Entry;

import static javafx.application.ConditionalFeature.SWT;

*
 *  货车的界面渲染


public class TruckRenderer extends AbstractCanvasRenderer {

    static final int ROUND_RECT_ARC_HEIGHT = 5;
    static final int X_OFFSET = -5;
    static final int Y_OFFSET = -30;

    enum Language {
        DUTCH("INSTAPPEN", "UITSTAPPEN"), ENGLISH("EMBARK", "DISEMBARK"),CHINESE("装货中", "卸货中");
        final String embark;
        final String disembark;
        Language(String s1, String s2) {
            embark = s1;
            disembark = s2;
        }
    }
    final RoadModel roadModel;
    final PDPModel pdpModel;
    final Language lang;

    TruckRenderer(RoadModel r, PDPModel p, Language l) {
        lang = l;
        roadModel = r;
        pdpModel = p;
    }

    @Override
    public void renderStatic(GC gc, ViewPort vp) {}
    enum Pred implements Predicate<Entry<RoadUser, Point>> {
        INSTANCE {
            @Override
            public boolean apply(Entry<RoadUser, Point> input) {
                return input.getKey() instanceof Truck;
            }
        }
    }

    @Override
    public void renderDynamic(GC gc, ViewPort vp, long time) {
        final Map<RoadUser, Point> map =
                Maps.filterEntries(roadModel.getObjectsAndPositions(), Pred.INSTANCE);
        for (final Entry<RoadUser, Point> entry : map.entrySet()) {
            final Truck t = (Truck) entry.getKey();
            final Point p = entry.getValue();
            final int x = vp.toCoordX(p.x) + X_OFFSET;
            final int y = vp.toCoordY(p.y) + Y_OFFSET;
            final VehicleState vs = pdpModel.getVehicleState(t);
            String text = "";
            if (vs == VehicleState.DELIVERING) {
                text = lang.disembark;
            } else if (vs == VehicleState.PICKING_UP) {
                text = lang.embark;
            } else if (t.getMyToDoCargoNum()+t.getMyDoingCargoNum() > 0) {
                text = t.getID()+ "\n已接" + t.getMyToDoCargoNum()+" 在运"+ t.getMyDoingCargoNum();
            }
//            //车辆空闲状态 展示车牌
//            else if (t.getMyToDoCargoNum()+t.getMyDoingCargoNum() == 0) {
//                text = t.getID();
//            }
            final org.eclipse.swt.graphics.Point extent = gc.textExtent(text);
            gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_RED));
            gc.fillRoundRectangle(x - extent.x / 2, y - extent.y / 2,
                    extent.x + 2, extent.y + 2, ROUND_RECT_ARC_HEIGHT,
                    ROUND_RECT_ARC_HEIGHT);
            gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
            gc.drawText(text, x - extent.x / 2 + 1, y - extent.y / 2 + 1,
                    true);
        }
    }

    static Builder builder(Language l) {
        return new AutoValue_TruckRenderer_Builder(l);
    }
   @AutoValue
    abstract static class Builder extends
           AbstractModelBuilder<TruckRenderer, Void> {
        private static final long serialVersionUID = -1772420262312399129L;
        Builder() {
            setDependencies(RoadModel.class, PDPModel.class);
        }
        abstract Language language();
        @Override
        public TruckRenderer build(DependencyProvider dependencyProvider) {
            final RoadModel rm = dependencyProvider.get(RoadModel.class);
            final PDPModel pm = dependencyProvider.get(PDPModel.class);
            return new TruckRenderer(rm, pm, language());
        }
    }
}
