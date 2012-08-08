/*
 * Copyright 2012 stuart.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.browsermob.proxy.bricks;

import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import org.browsermob.proxy.util.Log;

/**
 *
 * @author stuart
 */
public class BaseBrick {

    protected static RestConfig restConfig = RestConfig.getInstance();

    protected static final Log LOG = new Log();


    public static class BrickEmptySuccessReply {
        private boolean success = true;

        public boolean getSuccess() {
            return this.success;
        }
    }

    public static class BrickSuccessReply {
        private boolean success = true;
        private Object data = null;

        public BrickSuccessReply() {
        }

        public boolean getSuccess() {
            return this.success;
        }

        public Object getData() {
            return this.data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    public static class BrickErrorReply {
        private boolean error = true;
        private Object  data  = null;

        public BrickErrorReply() {
        }

        public boolean getError() {
            return this.error;
        }

        public Object getData() {
            return this.data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }

    protected Reply<?> wrapSuccess(Object data) {
        if (!this.getEnhancedReplies()) {
            return Reply.with(data).as(Json.class);
        }

        BrickSuccessReply restReply = new BrickSuccessReply();
        restReply.setData(data);

        return Reply.with(restReply).as(Json.class);
    }

    protected Reply<?> wrapEmptySuccess() {
        if (!this.getEnhancedReplies()) {
            return Reply.saying().ok();
        }

        return Reply.with(new BrickEmptySuccessReply()).as(Json.class);
    }

    protected Reply<?> wrapError(Object data) {
        if (!this.getEnhancedReplies()) {
            return Reply.with(data).as(Json.class).error();
        }

        BrickErrorReply restReply = new BrickErrorReply();
        restReply.setData(data);

        return Reply.with(restReply).as(Json.class).error();
    }

    protected boolean getEnhancedReplies() {
        return restConfig.getEnhancedReplies();
    }

    protected void logParam(String name, Object param) {
        if (!restConfig.getParamLogs()) {
            return;
        }

        LOG.info("  PARAM: %s = %s", name, param);
    }
}
