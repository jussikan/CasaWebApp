package fi.casa.webapp;

import android.view.View;

import androidx.test.espresso.action.CoordinatesProvider;

public enum MockPageLinkPosition implements CoordinatesProvider {
    ALINK1 {
        @Override
        public float[] calculateCoordinates(View view) {
            final int[] location = new int[2];
            view.getLocationOnScreen(location);

            final float[] coordinates = {
                    location[0] + 1,
                    location[1] + 1
            };
            return coordinates;
        }
    },
    ALINK2 {
        @Override
        public float[] calculateCoordinates(View view) {
            final int[] location = new int[2];
            view.getLocationOnScreen(location);

            final float[] coordinates = {
                    location[0] + 1,
                    location[1] + 21
            };
            return coordinates;
        }
    },
    FLINK1 {
        @Override
        public float[] calculateCoordinates(View view) {
            final int[] location = new int[2];
            view.getLocationOnScreen(location);

            final float[] coordinates = {
                    location[0] + 1,
                    location[1] + 41
            };
            return coordinates;
        }
    },
    TARGET {
        @Override
        public float[] calculateCoordinates(View view) {
            final int[] location = new int[2];
            view.getLocationOnScreen(location);

            float[] coordinates = {
                    location[0] + 1,
                    location[1] + 1
            };
            return coordinates;
        }
    }
}
