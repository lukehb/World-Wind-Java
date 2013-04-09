/*
 Copyright (C) 2013 United States Government as represented by the Administrator of the
 National Aeronautics and Space Administration. All Rights Reserved.
 
 @version $Id$
 */

#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import <OpenGLES/ES2/gl.h>
#import "WorldWind/Util/WWDisposable.h"

@class WWSceneController;
@protocol WWNavigator;

@interface WorldWindView : UIView <WWDisposable>

@property (readonly, nonatomic) GLuint frameBuffer;
@property (readonly, nonatomic) GLuint colorBuffer;
@property (readonly, nonatomic) GLuint depthBuffer;
@property (readonly, nonatomic) CGRect viewport;
@property (readonly, nonatomic) EAGLContext* context;
@property (readonly, nonatomic) WWSceneController* sceneController;
@property (readonly, nonatomic) id<WWNavigator> navigator;
@property BOOL redrawRequested;

- (void) drawView;
- (void) tearDownGL;
- (void) handleNotification:(NSNotification*)notification;

@end