#include <pebble.h>

Window *window;
TextLayer *text_layer;
Layer *path_layer;
unsigned int uptime = 0;
bool running = 0;
static char str[80] = "";
DictionaryIterator *iter;

static GPath *s_my_path_ptr = NULL;
static const GPathInfo TRIANGLE_NEXT = {
  3,
  (GPoint []) {
    {-8, -8},
    {-8, 8},
    {6, 0},
  }
};

static const GPathInfo TRIANGLE_PREV = {
  3,
  (GPoint []) {
    {6, -8},
    {6, 8},
    {-8, 0},
  }
};

static GPath *path_next = NULL;
static GPath *path_prev = NULL;

void out_sent_handler(DictionaryIterator *sent, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, "OK");
}


void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, "FAILED");
}


void in_received_handler(DictionaryIterator *received, void *context) {
  // incoming message received
}


void in_dropped_handler(AppMessageResult reason, void *context) {
  // incoming message dropped
}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  running = !running;
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  app_message_outbox_begin(&iter);
  dict_write_uint8(iter, 1, 0);
  app_message_outbox_send();
  APP_LOG(APP_LOG_LEVEL_DEBUG, "prev");
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  app_message_outbox_begin(&iter);
  dict_write_uint8(iter, 1, 1);
  app_message_outbox_send();
  APP_LOG(APP_LOG_LEVEL_DEBUG, "next");
}

static void accel_tap_handler(AccelAxisType axis, int32_t direction) {
  app_message_outbox_begin(&iter);
  dict_write_uint8(iter, 1, 1);
  app_message_outbox_send();
  APP_LOG(APP_LOG_LEVEL_DEBUG, "next");
}

static void config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  accel_tap_service_subscribe(accel_tap_handler);
}


static void path_layer_update_callback(Layer *me, GContext *ctx) {
  (void)me;
  // draw filled uses the fill color
  graphics_context_set_fill_color(ctx, GColorBlack);
  gpath_draw_filled(ctx, path_next);
  gpath_draw_filled(ctx, path_prev);
}

static void handle_sec_tick(struct tm *tick_time, TimeUnits units_changed) {
   
  if (running)
  {
    uptime++;
    
    unsigned int min = uptime/60;
    unsigned int sec = uptime-min*60;
    
    if (sec < 10)
    {
      if (min < 10)
      {
        snprintf(str, sizeof(str), "0%u:0%u", min, sec);
      }
      else
      {
        snprintf(str, sizeof(str), "%u:0%u", min, sec);
      }
    }
    else
    {
      if (min < 10)
      {
        snprintf(str, sizeof(str), "0%u:%u", min, sec);
      }
      else
      {
        snprintf(str, sizeof(str), "%u:%u", min, sec);
      }
    }
    
    text_layer_set_text(text_layer, str);
  }
}

void handle_init(void) {
	// Create a window and text layer
	window = window_create();
  window_set_click_config_provider(window, config_provider);

  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_frame(window_layer);

  path_layer = layer_create(bounds);
  layer_set_update_proc(path_layer, path_layer_update_callback);
  layer_add_child(window_layer, path_layer);
  
  tick_timer_service_subscribe(SECOND_UNIT, handle_sec_tick);
  
	// Add the text layer to the window
  text_layer = text_layer_create(GRect(0, bounds.size.h/2-30, bounds.size.w, bounds.size.h/2-5));
  text_layer_set_font(text_layer, fonts_get_system_font(FONT_KEY_BITHAM_42_BOLD));
	text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
  text_layer_set_text(text_layer, "00:00");

	layer_add_child(window_layer, text_layer_get_layer(text_layer));

  path_next = gpath_create(&TRIANGLE_NEXT);
  gpath_move_to(path_next, GPoint(bounds.size.w-10, bounds.size.h/2+65));
  path_prev = gpath_create(&TRIANGLE_PREV);
  gpath_move_to(path_prev, GPoint(bounds.size.w-10, bounds.size.h/2-65));
  
  app_message_register_inbox_received(in_received_handler);
  app_message_register_inbox_dropped(in_dropped_handler);
  app_message_register_outbox_sent(out_sent_handler);
  app_message_register_outbox_failed(out_failed_handler);
  
  const uint32_t inbound_size = 64;
  const uint32_t outbound_size = 64;
  app_message_open(inbound_size, outbound_size);
  
	// Push the window
	window_stack_push(window, true);
	
	// App Logging!
	APP_LOG(APP_LOG_LEVEL_DEBUG, "gSlides started");
}

void handle_deinit(void) {
  
  gpath_destroy(path_next);
  gpath_destroy(path_prev);
  
	// Destroy the text layer
	text_layer_destroy(text_layer);
  layer_destroy(path_layer);
    
	// Destroy the window
	window_destroy(window);
}

int main(void) {
	handle_init();
	app_event_loop();
	handle_deinit();
}
