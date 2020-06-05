package com.luck.picture.lib.instagram.filter;

import android.content.Context;

import com.luck.picture.lib.R;


/**
 * Created by sam on 14-8-9.
 */
public class IFRiseFilter extends IFImageFilter {
    private static final String SHADER = "\n precision lowp float;\n\n " +
            "varying highp vec2 textureCoordinate;\n\n " +
            "uniform sampler2D inputImageTexture;\n " +
            "uniform sampler2D inputImageTexture2;\n " +
            "uniform sampler2D inputImageTexture3;\n " +
            "uniform sampler2D inputImageTexture4;\n\n " +
            "void main()\n  " +
            "{\n\n  " +
            "vec4 texel = texture2D(inputImageTexture, textureCoordinate);\n   " +
            "vec3 bbTexel = texture2D(inputImageTexture2, textureCoordinate).rgb;\n\n   " +
            "texel.r = texture2D(inputImageTexture3, vec2(bbTexel.r, texel.r)).r;\n   " +
            "texel.g = texture2D(inputImageTexture3, vec2(bbTexel.g, texel.g)).g;\n   " +
            "texel.b = texture2D(inputImageTexture3, vec2(bbTexel.b, texel.b)).b;\n\n   " +
            "vec4 mapped;\n   mapped.r = texture2D(inputImageTexture4, vec2(texel.r, .16666)).r;\n   " +
            "mapped.g = texture2D(inputImageTexture4, vec2(texel.g, .5)).g;\n   " +
            "mapped.b = texture2D(inputImageTexture4, vec2(texel.b, .83333)).b;\n   " +
            "mapped.a = 1.0;\n\n   " +
            "gl_FragColor = mapped;\n }";

    public IFRiseFilter(Context paramContext) {
        super(paramContext, SHADER);
        setRes();
    }

    private void setRes() {
        addInputTexture(R.drawable.blackboard);
        addInputTexture(R.drawable.overlay_map);
        addInputTexture(R.drawable.rise_map);
    }
}
