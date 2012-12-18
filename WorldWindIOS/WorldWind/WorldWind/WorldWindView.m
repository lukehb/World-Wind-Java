/*
 Copyright (C) 2013 United States Government as represented by the Administrator of the
 National Aeronautics and Space Administration. All Rights Reserved.
 
 @version $Id$
 */

#import "WorldWind/WorldWindView.h"
#import "WorldWind/Render/WWSceneController.h"
#import "WorldWind/Navigate/WWBasicNavigator.h"
#import "WorldWind/WWLog.h"

@implementation WorldWindView

+ (Class) layerClass
{
    return [CAEAGLLayer class];
}

- (id) initWithFrame:(CGRect) frame
{
    if (self = [super initWithFrame:frame])
    {
        CAEAGLLayer *eaglLayer = (CAEAGLLayer *) super.layer;
        eaglLayer.opaque = YES;

        self->_context = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
        [EAGLContext setCurrentContext:self.context];

        // Generate OpenGL objects for the framebuffer, color renderbuffer, and depth renderbuffer. The storage for
        // each renderbuffer is allocated in resizeWithLayer.
        glGenFramebuffers(1, &self->_frameBuffer);
        glGenRenderbuffers(1, &self->_renderBuffer);
        glGenRenderbuffers(1, &self->_depthBuffer);

        // Allocate storage for the color and depth renderbuffers. This computes the correct and consistent dimensions
        // for the renderbuffers, and assigns the viewport property.
        [self resizeWithLayer:eaglLayer];

        // Configure the framebuffer's color and depth attachments, then validate the framebuffer's status.
        glBindFramebuffer(GL_FRAMEBUFFER, self->_frameBuffer);
        glBindRenderbuffer(GL_RENDERBUFFER, self->_renderBuffer);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, self->_renderBuffer);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, self->_depthBuffer);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
        {
            WWLog(@"Failed to complete framebuffer attachment %x",
                  glCheckFramebufferStatus(GL_FRAMEBUFFER));
            return nil;
        }

        self->_sceneController = [[WWSceneController alloc] init];
        self->_navigator = [[WWBasicNavigator alloc] initWithView:self];
    }

    return self;
}

- (void) resizeWithLayer:(CAEAGLLayer*) layer
{
    GLint width, height;

    // Allocate storage for the color renderbuffer using the CAEAGLLayer, then retrieve its dimensions. The color
    // renderbuffer's are calculated based on this view's bounds and scale factor, and therefore may not be equal to
    // this view's bounds. The depth renderbuffer and viewport must have the same dimensions as the color renderbuffer.
    glBindRenderbuffer(GL_RENDERBUFFER, self->_renderBuffer);
    [self->_context renderbufferStorage:GL_RENDERBUFFER fromDrawable:layer];
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_WIDTH, &width);
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_HEIGHT, &height);

    // Allocate storage for the depth renderbuffer using the color renderbuffer's dimensions retrieved from OpenGL. The
    // color renderbuffer and the depth renderbuffer must have the same dimensions.
    glBindRenderbuffer(GL_RENDERBUFFER, self->_depthBuffer);
    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24_OES, (GLsizei) width, (GLsizei) height);

    // Restore the GL_RENDERBUFFER binding to the color renderbuffer. All other methods in WorldWindView assume that the
    // color renderbuffer is bound.
    glBindRenderbuffer(GL_RENDERBUFFER, self->_renderBuffer);

    // Assign the viewport to a rectangle with its origin at (0, 0) and with dimensions equal to the color renderbuffer.
    // This viewport property is used by the scene controller to set the OpenGL viewport state, and is used by the
    // navigator to compute the projection matrix. Both must use the dimensions of the color renderbuffer.
    self->_viewport = CGRectMake(0, 0, width, height);
}

- (void) drawView
{
    [EAGLContext setCurrentContext:self.context];

    // The scene controller catches and logs rendering exceptions, so don't do it here. Draw the scene using the current
    // OpenGL viewport. We use the viewport instead of the bounds because the viewport contains the actual render buffer
    // dimension, whereas the bounds contain this view's dimension in screen points. When a WorldWindView is configured
    // for a retina display, the bounds do not represent the actual OpenGL render buffer resolution.
    [self.sceneController setNavigatorState:[[self navigator] currentState]];
    [self.sceneController render:self.viewport];

    // Requests that Core Animation display the renderbuffer currently bound to GL_RENDERBUFFER. This assumes that the
    // color renderbuffer is currently bound.
    [self.context presentRenderbuffer:GL_RENDERBUFFER];
}

- (void) layoutSubviews
{
    // Called whenever the backing Core Animation layer's bounds or properties change. In this case, the WorldWindView
    // must reallocate storage for the color and depth renderbuffers, and reassign the viewport property. This ensures
    // that the renderbuffers fit the view, and that the OpenGL viewport and projection matrix match the renderbuffer
    // dimensions.

    CAEAGLLayer* eaglLayer = (CAEAGLLayer*) super.layer;
    [self resizeWithLayer:eaglLayer];

    [self drawView];
}

- (void) dealloc
{
    [EAGLContext setCurrentContext:self.context];

    [self.sceneController dispose];

    [self tearDownGL];

    if ([EAGLContext currentContext] == self.context)
        [EAGLContext setCurrentContext:nil];
}

- (void) tearDownGL
{
    glDeleteRenderbuffers(1, &self->_renderBuffer);
    self->_renderBuffer = 0;

    glDeleteRenderbuffers(1, &self->_depthBuffer);
    self->_depthBuffer = 0;

    glDeleteFramebuffers(1, &self->_frameBuffer);
    self->_frameBuffer = 0;
}

@end